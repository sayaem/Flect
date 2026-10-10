package com.example.model

/**
 * Calculated bounded corrections produced by Flect's Adaptive Engine.
 * Intelligently compensates for over-warmth, harsh contrast, crushed shadows,
 * or highlight blowouts without violating the selected preset's aesthetic character.
 */
data class AdaptiveAdjustments(
    val exposureOffset: Float = 0.0f,
    val contrastOffset: Float = 0.0f,
    val warmthOffset: Float = 0.0f,
    val saturationOffset: Float = 0.0f,
    val highlightCompression: Float = 0.0f, // negative or zero
    val shadowRescue: Float = 0.0f,          // positive or zero
    val insightReason: String = "Balanced exposure & dynamic range"
) {
    val isEngaged: Boolean
        get() = exposureOffset != 0f || contrastOffset != 0f || warmthOffset != 0f ||
                saturationOffset != 0f || highlightCompression != 0f || shadowRescue != 0f

    companion object {
        val ZERO = AdaptiveAdjustments()
    }
}
