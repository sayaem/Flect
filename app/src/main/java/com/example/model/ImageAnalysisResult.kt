package com.example.model

/**
 * Statistical analysis of a photograph's tonal and color characteristics.
 * Calculated locally on a downsampled representation (e.g. 128x128).
 */
data class ImageAnalysisResult(
    val meanLuminance: Float,       // 0.0 (pitch black) .. 1.0 (pure white)
    val p10Shadows: Float,          // 10th percentile luminance (shadow depth)
    val p50Midtones: Float,         // Median luminance
    val p90Highlights: Float,       // 90th percentile luminance (highlight ceiling)
    val contrastStdDev: Float,      // Standard deviation of luminance (dynamic contrast)
    val meanSaturation: Float,      // Average HSV saturation (0.0 .. 1.0)
    val warmCoolBias: Float,        // -1.0 (heavily cool/blue) .. +1.0 (heavily warm/amber)
    val highlightClippingRisk: Float, // Fraction of pixels with luminance > 0.94
    val shadowClippingRisk: Float,    // Fraction of pixels with luminance < 0.06
    val dominantTone: DominantTone = DominantTone.BALANCED
) {
    enum class DominantTone {
        BALANCED,
        WARM_GOLDEN,
        COOL_SHADOW,
        HIGH_CONTRAST,
        LOW_CONTRAST_FLAT,
        LOW_LIGHT_NIGHT,
        BRIGHT_HIGH_KEY
    }

    companion object {
        val DEFAULT = ImageAnalysisResult(
            meanLuminance = 0.5f,
            p10Shadows = 0.15f,
            p50Midtones = 0.5f,
            p90Highlights = 0.85f,
            contrastStdDev = 0.2f,
            meanSaturation = 0.4f,
            warmCoolBias = 0.0f,
            highlightClippingRisk = 0.02f,
            shadowClippingRisk = 0.02f,
            dominantTone = DominantTone.BALANCED
        )
    }
}
