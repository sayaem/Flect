package com.example.model

enum class PresetCategory {
    ALL,
    ESSENTIALS,
    CINEMATIC,
    FILM,
    MOOD
}

data class AdaptiveLimits(
    val maxExposureOffset: Float = 0.15f,
    val maxWarmthOffset: Float = 0.20f,
    val maxContrastOffset: Float = 0.15f,
    val shadowRescueLimit: Float = 0.25f,
    val highlightProtectionLimit: Float = 0.30f
)

data class FilterPreset(
    val id: String,
    val name: String,
    val tagline: String,
    val category: PresetCategory,
    val version: Int = 1,
    val defaultIntensity: Float = 1.0f,
    // Baseline preset parameters
    val exposure: Float = 0.0f,         // -1.0 to 1.0 (typical range -0.3 to +0.3)
    val contrast: Float = 0.0f,         // -1.0 to 1.0 (typical range -0.2 to +0.4)
    val saturation: Float = 0.0f,       // -1.0 to 1.0 (typical range -0.5 to +0.5)
    val warmth: Float = 0.0f,           // -1.0 to 1.0 (-0.4 cool to +0.4 warm)
    val tint: Float = 0.0f,             // -1.0 to 1.0 (green to magenta)
    val highlights: Float = 0.0f,       // -1.0 to 1.0 (compress to boost)
    val shadows: Float = 0.0f,          // -1.0 to 1.0 (crush to lift)
    val blackPointLift: Float = 0.0f,   // 0.0 to 0.25 (matte / faded blacks)
    val vignette: Float = 0.0f,         // 0.0 to 0.8
    val grain: Float = 0.0f,            // 0.0 to 0.4
    val isMonochrome: Boolean = false,
    val rMix: Float = 1.0f,             // RGB channel matrix fine weights
    val gMix: Float = 1.0f,
    val bMix: Float = 1.0f,
    val adaptiveLimits: AdaptiveLimits = AdaptiveLimits()
)
