package com.example.engine

import android.graphics.Bitmap
import android.graphics.Color
import com.example.model.ImageAnalysisResult
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object ImageAnalyzer {

    /**
     * Analyzes image statistics quickly on a downsampled grid (e.g. 128x128).
     * Deterministic, thread-safe, and offline.
     */
    fun analyze(bitmap: Bitmap): ImageAnalysisResult {
        val sampleSize = 128
        val sampleBitmap = if (bitmap.width <= sampleSize && bitmap.height <= sampleSize) {
            bitmap
        } else {
            Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, true)
        }

        val width = sampleBitmap.width
        val height = sampleBitmap.height
        val totalPixels = width * height
        val pixels = IntArray(totalPixels)
        sampleBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        if (sampleBitmap != bitmap) {
            sampleBitmap.recycle()
        }

        val luminances = FloatArray(totalPixels)
        var sumLum = 0.0
        var sumSat = 0.0
        var sumR = 0.0
        var sumB = 0.0
        var clippedHighCount = 0
        var clippedLowCount = 0

        for (i in 0 until totalPixels) {
            val color = pixels[i]
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)

            // Rec.601 / sRGB luminance
            val lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255.0f
            luminances[i] = lum
            sumLum += lum
            sumR += r
            sumB += b

            if (lum > 0.94f) clippedHighCount++
            if (lum < 0.06f) clippedLowCount++

            // Fast saturation approximation: (max - min) / max
            val cMax = max(r, max(g, b))
            val cMin = min(r, min(g, b))
            val sat = if (cMax == 0) 0.0f else (cMax - cMin).toFloat() / cMax.toFloat()
            sumSat += sat
        }

        val meanLum = (sumLum / totalPixels).toFloat().coerceIn(0.0f, 1.0f)
        val meanSat = (sumSat / totalPixels).toFloat().coerceIn(0.0f, 1.0f)
        val warmCoolBias = ((sumR - sumB) / (totalPixels * 255.0)).toFloat().coerceIn(-1.0f, 1.0f)

        // Variance / Standard Deviation for dynamic contrast
        var varianceSum = 0.0
        for (lum in luminances) {
            val diff = lum - meanLum
            varianceSum += (diff * diff)
        }
        val contrastStdDev = sqrt(varianceSum / totalPixels).toFloat()

        // Percentiles: p10 (shadows), p50 (midtones), p90 (highlights)
        luminances.sort()
        val p10 = luminances[(totalPixels * 0.10f).toInt().coerceIn(0, totalPixels - 1)]
        val p50 = luminances[(totalPixels * 0.50f).toInt().coerceIn(0, totalPixels - 1)]
        val p90 = luminances[(totalPixels * 0.90f).toInt().coerceIn(0, totalPixels - 1)]

        val highlightClipping = clippedHighCount.toFloat() / totalPixels.toFloat()
        val shadowClipping = clippedLowCount.toFloat() / totalPixels.toFloat()

        val dominantTone = when {
            meanLum < 0.30f -> ImageAnalysisResult.DominantTone.LOW_LIGHT_NIGHT
            meanLum > 0.72f -> ImageAnalysisResult.DominantTone.BRIGHT_HIGH_KEY
            warmCoolBias > 0.12f -> ImageAnalysisResult.DominantTone.WARM_GOLDEN
            warmCoolBias < -0.12f -> ImageAnalysisResult.DominantTone.COOL_SHADOW
            contrastStdDev > 0.25f -> ImageAnalysisResult.DominantTone.HIGH_CONTRAST
            contrastStdDev < 0.13f -> ImageAnalysisResult.DominantTone.LOW_CONTRAST_FLAT
            else -> ImageAnalysisResult.DominantTone.BALANCED
        }

        return ImageAnalysisResult(
            meanLuminance = meanLum,
            p10Shadows = p10,
            p50Midtones = p50,
            p90Highlights = p90,
            contrastStdDev = contrastStdDev,
            meanSaturation = meanSat,
            warmCoolBias = warmCoolBias,
            highlightClippingRisk = highlightClipping,
            shadowClippingRisk = shadowClipping,
            dominantTone = dominantTone
        )
    }
}
