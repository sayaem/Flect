package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.example.model.AdaptiveAdjustments
import com.example.model.EditParameters
import com.example.model.FilterPreset
import com.example.model.ImageAnalysisResult
import java.util.Random
import kotlin.math.hypot
import kotlin.math.max

object FilterRenderer {

    /**
     * Builds the unified ColorMatrix incorporating preset baseline, adaptive compensations,
     * intensity interpolation, and user manual adjustments.
     */
    fun buildColorMatrix(
        parameters: EditParameters,
        analysis: ImageAnalysisResult,
        preset: FilterPreset?
    ): ColorMatrix {
        val adaptive = if (parameters.adaptiveEnabled && preset != null) {
            AdaptiveEngine.computeAdjustments(analysis, preset)
        } else {
            AdaptiveAdjustments.ZERO
        }

        val intensity = parameters.presetIntensity.coerceIn(0.0f, 1.0f)

        // Calculate combined effective values
        val presetWarmth = (preset?.warmth ?: 0.0f) * intensity
        val effectiveWarmth = (presetWarmth + adaptive.warmthOffset + parameters.manualWarmth).coerceIn(-1.0f, 1.0f)

        val presetTint = (preset?.tint ?: 0.0f) * intensity
        val effectiveTint = (presetTint + parameters.manualTintSafe()).coerceIn(-1.0f, 1.0f)

        val presetExposure = (preset?.exposure ?: 0.0f) * intensity
        val effectiveExposure = (presetExposure + adaptive.exposureOffset + parameters.manualExposure).coerceIn(-1.0f, 1.0f)

        val presetContrast = (preset?.contrast ?: 0.0f) * intensity
        val effectiveContrast = (presetContrast + adaptive.contrastOffset + parameters.manualContrast).coerceIn(-1.0f, 1.0f)

        val presetSaturation = (preset?.saturation ?: 0.0f) * intensity
        val effectiveSaturation = (presetSaturation + adaptive.saturationOffset + parameters.manualSaturation).coerceIn(-1.0f, 2.0f)

        val presetHighlights = (preset?.highlights ?: 0.0f) * intensity
        val effectiveHighlights = (presetHighlights + adaptive.highlightCompression + parameters.manualHighlights).coerceIn(-1.0f, 1.0f)

        val presetShadows = (preset?.shadows ?: 0.0f) * intensity
        val effectiveShadows = (presetShadows + adaptive.shadowRescue + parameters.manualShadows).coerceIn(-1.0f, 1.0f)

        val blackLift = ((preset?.blackPointLift ?: 0.0f) * intensity).coerceIn(0.0f, 0.35f)

        // 1. Base Channel Matrix (monochrome / channel mix / warmth / tint)
        val matrix = ColorMatrix()

        val isMono = preset?.isMonochrome == true
        if (isMono) {
            val monoWeight = intensity
            val colorWeight = 1.0f - monoWeight
            val rLum = 0.299f * monoWeight
            val gLum = 0.587f * monoWeight
            val bLum = 0.114f * monoWeight

            val monoArray = floatArrayOf(
                rLum + colorWeight, gLum, bLum, 0f, 0f,
                rLum, gLum + colorWeight, bLum, 0f, 0f,
                rLum, gLum, bLum + colorWeight, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            matrix.set(monoArray)
        } else {
            // Apply RGB channel mix
            val rMix = 1.0f + ((preset?.rMix ?: 1.0f) - 1.0f) * intensity
            val gMix = 1.0f + ((preset?.gMix ?: 1.0f) - 1.0f) * intensity
            val bMix = 1.0f + ((preset?.bMix ?: 1.0f) - 1.0f) * intensity

            // Warmth shifts Red up, Blue down; Tint shifts Green
            val rWarmth = 1.0f + (effectiveWarmth * 0.16f) + (effectiveTint * 0.06f)
            val gWarmth = 1.0f + (effectiveWarmth * 0.04f) - (effectiveTint * 0.12f)
            val bWarmth = 1.0f - (effectiveWarmth * 0.18f) + (effectiveTint * 0.06f)

            val colorScaleArray = floatArrayOf(
                rMix * rWarmth, 0f, 0f, 0f, 0f,
                0f, gMix * gWarmth, 0f, 0f, 0f,
                0f, 0f, bMix * bWarmth, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            matrix.set(colorScaleArray)
        }

        // 2. Saturation
        if (effectiveSaturation != 0.0f && !isMono) {
            val satMatrix = ColorMatrix()
            val satFactor = (1.0f + effectiveSaturation).coerceAtLeast(0.0f)
            satMatrix.setSaturation(satFactor)
            matrix.postConcat(satMatrix)
        }

        // 3. Contrast, Black Point Lift, Shadows & Highlights
        val contrastFactor = (1.0f + effectiveContrast).coerceIn(0.2f, 2.5f)
        val scale = contrastFactor * (1.0f - blackLift)
        // Midpoint centering for contrast + black lift + shadow lift + highlight compression
        val shadowOffset = effectiveShadows * 32.0f
        val highlightOffset = effectiveHighlights * 18.0f
        val exposureOffset = effectiveExposure * 45.0f

        val finalOffset = 128.0f * (1.0f - contrastFactor) + (blackLift * 255.0f) +
                shadowOffset + highlightOffset + exposureOffset

        val tonalMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, finalOffset,
                0f, scale, 0f, 0f, finalOffset,
                0f, 0f, scale, 0f, finalOffset,
                0f, 0f, 0f, 1f, 0f
            )
        )
        matrix.postConcat(tonalMatrix)

        return matrix
    }

    /**
     * Renders a filtered bitmap given the source bitmap and editing parameters.
     * Guaranteed deterministic and non-destructive.
     */
    fun render(
        sourceBitmap: Bitmap,
        parameters: EditParameters,
        analysis: ImageAnalysisResult,
        preset: FilterPreset? = PresetLibrary.getById(parameters.presetId)
    ): Bitmap {
        val width = sourceBitmap.width
        val height = sourceBitmap.height

        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)

        // 1. Primary Color & Tone pass via ColorMatrix
        val colorMatrix = buildColorMatrix(parameters, analysis, preset)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        canvas.drawBitmap(sourceBitmap, 0f, 0f, paint)

        // 2. Vignette pass
        val intensity = parameters.presetIntensity.coerceIn(0.0f, 1.0f)
        val vignetteAmount = ((preset?.vignette ?: 0.0f) * intensity + parameters.manualVignette)
            .coerceIn(0.0f, 1.0f)

        if (vignetteAmount > 0.02f) {
            drawVignette(canvas, width, height, vignetteAmount)
        }

        // 3. Film Grain pass (subtle deterministic noise)
        val grainAmount = ((preset?.grain ?: 0.0f) * intensity + parameters.manualGrain)
            .coerceIn(0.0f, 1.0f)

        if (grainAmount > 0.02f) {
            drawFilmGrain(canvas, width, height, grainAmount)
        }

        return outputBitmap
    }

    private fun drawVignette(canvas: Canvas, width: Int, height: Int, amount: Float) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = hypot(cx, cy)

        val maxAlpha = (amount * 190.0f).toInt().coerceIn(0, 240)
        val colors = intArrayOf(
            Color.TRANSPARENT,
            Color.argb((maxAlpha * 0.3f).toInt(), 0, 0, 0),
            Color.argb(maxAlpha, 0, 0, 0)
        )
        val stops = floatArrayOf(0.40f, 0.75f, 1.0f)

        val shader = RadialGradient(cx, cy, radius, colors, stops, Shader.TileMode.CLAMP)
        val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.shader = shader
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)
    }

    private fun drawFilmGrain(canvas: Canvas, width: Int, height: Int, amount: Float) {
        // High-performance deterministic micro-noise
        val random = Random(42L) // Deterministic seed for reproducible previews & exports
        val grainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val alphaBase = (amount * 45.0f).toInt().coerceIn(4, 70)

        // Sample sparsely across grid to keep export fast
        val step = max(2, (width / 500))
        val samplePoints = (width / step) * (height / step)
        val density = (samplePoints * amount * 0.4f).toInt().coerceAtMost(25000)

        val pts = FloatArray(density * 2)
        for (i in 0 until density) {
            pts[i * 2] = random.nextInt(width).toFloat()
            pts[i * 2 + 1] = random.nextInt(height).toFloat()
        }

        val isDarkGrain = random.nextBoolean()
        grainPaint.color = if (isDarkGrain) Color.argb(alphaBase, 0, 0, 0) else Color.argb(alphaBase, 255, 255, 255)
        grainPaint.strokeWidth = 1.5f
        canvas.drawPoints(pts, grainPaint)
    }

    private fun EditParameters.manualTintSafe(): Float = 0.0f
}
