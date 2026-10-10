package com.example.model

/**
 * Immutable parameters defining all visual transformations for an edit session.
 */
data class EditParameters(
    val presetId: String? = null,
    val presetIntensity: Float = 1.0f,
    val adaptiveEnabled: Boolean = true,
    // Manual adjustments (-1.0f to 1.0f, except vignette and grain 0.0f to 1.0f)
    val manualExposure: Float = 0.0f,
    val manualContrast: Float = 0.0f,
    val manualSaturation: Float = 0.0f,
    val manualWarmth: Float = 0.0f,
    val manualHighlights: Float = 0.0f,
    val manualShadows: Float = 0.0f,
    val manualVignette: Float = 0.0f,
    val manualGrain: Float = 0.0f
) {
    val hasManualAdjustments: Boolean
        get() = manualExposure != 0f || manualContrast != 0f || manualSaturation != 0f ||
                manualWarmth != 0f || manualHighlights != 0f || manualShadows != 0f ||
                manualVignette != 0f || manualGrain != 0f

    val isOriginal: Boolean
        get() = presetId == null && !hasManualAdjustments

    fun resetAll(): EditParameters = EditParameters()

    fun resetManual(): EditParameters = copy(
        manualExposure = 0f,
        manualContrast = 0f,
        manualSaturation = 0f,
        manualWarmth = 0f,
        manualHighlights = 0f,
        manualShadows = 0f,
        manualVignette = 0f,
        manualGrain = 0f
    )

    companion object {
        val ORIGINAL = EditParameters()
    }
}
