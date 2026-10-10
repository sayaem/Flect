package com.example.engine

import com.example.model.AdaptiveAdjustments
import com.example.model.FilterPreset
import com.example.model.ImageAnalysisResult
import kotlin.math.abs

object AdaptiveEngine {

    /**
     * Calculates bounded corrections to harmonize preset styling with image characteristics.
     * Prevents blown highlights, orange skin tones, or crushed shadows.
     */
    fun computeAdjustments(
        analysis: ImageAnalysisResult,
        preset: FilterPreset?
    ): AdaptiveAdjustments {
        if (preset == null) return AdaptiveAdjustments.ZERO

        val limits = preset.adaptiveLimits
        val reasons = mutableListOf<String>()

        // 1. Warmth balance: Prevent orange blowout or unnatural blue tint
        var warmthOffset = 0.0f
        if (preset.warmth > 0.05f && analysis.warmCoolBias > 0.08f) {
            // Preset adds warmth, but image is already warm -> clamp excess warmth
            val excess = (analysis.warmCoolBias - 0.08f) * 1.2f
            warmthOffset = -(excess.coerceIn(0.0f, limits.maxWarmthOffset))
            if (abs(warmthOffset) > 0.03f) {
                reasons.add("Warmth clamped (${(warmthOffset * 100).toInt()}%) to preserve skin tones")
            }
        } else if (preset.warmth < -0.05f && analysis.warmCoolBias < -0.08f) {
            // Preset adds cool tone, but image is already cold -> soften cool shift
            val excess = (abs(analysis.warmCoolBias) - 0.08f) * 1.0f
            warmthOffset = excess.coerceIn(0.0f, limits.maxWarmthOffset)
            if (abs(warmthOffset) > 0.03f) {
                reasons.add("Cool tone moderated to keep neutrals balanced")
            }
        }

        // 2. Highlight protection & clipping prevention
        var highlightCompression = 0.0f
        if (analysis.p90Highlights > 0.82f || analysis.highlightClippingRisk > 0.03f) {
            val riskFactor = (analysis.p90Highlights - 0.80f) * 1.5f + (analysis.highlightClippingRisk * 2.0f)
            highlightCompression = -(riskFactor.coerceIn(0.0f, limits.highlightProtectionLimit))
            if (abs(highlightCompression) > 0.04f) {
                reasons.add("Highlight compression active (${(highlightCompression * 100).toInt()}%)")
            }
        }

        // 3. Shadow detail rescue
        var shadowRescue = 0.0f
        if (analysis.p10Shadows < 0.12f || analysis.shadowClippingRisk > 0.04f || analysis.meanLuminance < 0.35f) {
            val deepShadowFactor = (0.15f - analysis.p10Shadows).coerceAtLeast(0f) * 1.4f +
                    (analysis.shadowClippingRisk * 1.5f)
            shadowRescue = deepShadowFactor.coerceIn(0.0f, limits.shadowRescueLimit)
            if (shadowRescue > 0.04f) {
                reasons.add("Shadow details gently lifted (+${(shadowRescue * 100).toInt()}%)")
            }
        }

        // 4. Exposure compensation
        var exposureOffset = 0.0f
        if (analysis.meanLuminance < 0.30f) {
            // Underexposed image: gentle boost
            exposureOffset = ((0.35f - analysis.meanLuminance) * 0.4f)
                .coerceIn(0.0f, limits.maxExposureOffset)
            if (exposureOffset > 0.03f) {
                reasons.add("Exposure boosted (+${(exposureOffset * 100).toInt()}%)")
            }
        } else if (analysis.meanLuminance > 0.70f) {
            // Overexposed image: gentle pull down
            exposureOffset = -((analysis.meanLuminance - 0.68f) * 0.4f)
                .coerceIn(0.0f, limits.maxExposureOffset)
            if (abs(exposureOffset) > 0.03f) {
                reasons.add("Overexposure curbed (${(exposureOffset * 100).toInt()}%)")
            }
        }

        // 5. Dynamic contrast leveling
        var contrastOffset = 0.0f
        if (analysis.contrastStdDev > 0.26f && preset.contrast > 0.05f) {
            // Image already has harsh contrast -> moderate preset contrast
            contrastOffset = -((analysis.contrastStdDev - 0.24f) * 0.8f)
                .coerceIn(0.0f, limits.maxContrastOffset)
            if (abs(contrastOffset) > 0.03f) {
                reasons.add("Contrast softened to preserve midtone gradations")
            }
        } else if (analysis.contrastStdDev < 0.12f && preset.contrast < 0.0f) {
            // Flat image with matte preset -> keep punch
            contrastOffset = 0.06f
        }

        // 6. Saturation protection: if image is already super-saturated
        var saturationOffset = 0.0f
        if (analysis.meanSaturation > 0.65f && preset.saturation > 0.05f) {
            saturationOffset = -((analysis.meanSaturation - 0.60f) * 0.5f).coerceIn(0.0f, 0.20f)
            if (abs(saturationOffset) > 0.03f) {
                reasons.add("Color saturation protected from clipping")
            }
        }

        val reasonText = if (reasons.isEmpty()) {
            "Optimal balance: Natural scene distribution"
        } else {
            reasons.joinToString(" • ")
        }

        return AdaptiveAdjustments(
            exposureOffset = exposureOffset,
            contrastOffset = contrastOffset,
            warmthOffset = warmthOffset,
            saturationOffset = saturationOffset,
            highlightCompression = highlightCompression,
            shadowRescue = shadowRescue,
            insightReason = reasonText
        )
    }
}
