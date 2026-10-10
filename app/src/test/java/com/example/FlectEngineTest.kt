package com.example

import android.graphics.Bitmap
import android.graphics.Color
import com.example.engine.AdaptiveEngine
import com.example.engine.FilterRenderer
import com.example.engine.ImageAnalyzer
import com.example.engine.PresetLibrary
import com.example.model.EditParameters
import com.example.model.ImageAnalysisResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FlectEngineTest {

    @Test
    fun `preset library contains twelve distinct presets with valid parameters`() {
        val presets = PresetLibrary.allPresets
        assertEquals(12, presets.size)

        val ids = presets.map { it.id }.toSet()
        assertEquals("All 12 preset IDs must be unique", 12, ids.size)

        for (preset in presets) {
            assertTrue("Preset ${preset.id} name should not be blank", preset.name.isNotBlank())
            assertTrue("Preset ${preset.id} version should be >= 1", preset.version >= 1)
            assertTrue("Exposure within bounds", preset.exposure in -1f..1f)
            assertTrue("Contrast within bounds", preset.contrast in -1f..1f)
            assertTrue("Saturation within bounds", preset.saturation in -1f..1f)
            assertTrue("Warmth within bounds", preset.warmth in -1f..1f)
        }
    }

    @Test
    fun `adaptive engine clamps warmth for warm photos on warm presets`() {
        val goldenHour = PresetLibrary.getById("golden_hour")
        assertNotNull(goldenHour)

        // Simulate an already warm sunset photo (warmCoolBias = 0.25)
        val warmAnalysis = ImageAnalysisResult.DEFAULT.copy(
            warmCoolBias = 0.25f,
            dominantTone = ImageAnalysisResult.DominantTone.WARM_GOLDEN
        )

        val adjustments = AdaptiveEngine.computeAdjustments(warmAnalysis, goldenHour)
        assertTrue(
            "Adaptive engine must clamp excess warmth on already-warm photo",
            adjustments.warmthOffset < 0f
        )
        assertTrue(
            "Warmth clamp must respect preset limit",
            adjustments.warmthOffset >= -goldenHour!!.adaptiveLimits.maxWarmthOffset
        )
    }

    @Test
    fun `adaptive engine compresses highlights when highlight clipping risk is high`() {
        val vivid = PresetLibrary.getById("vivid")
        assertNotNull(vivid)

        val brightAnalysis = ImageAnalysisResult.DEFAULT.copy(
            p90Highlights = 0.95f,
            highlightClippingRisk = 0.12f,
            dominantTone = ImageAnalysisResult.DominantTone.BRIGHT_HIGH_KEY
        )

        val adjustments = AdaptiveEngine.computeAdjustments(brightAnalysis, vivid)
        assertTrue(
            "Highlight compression must be engaged",
            adjustments.highlightCompression < 0f
        )
    }

    @Test
    fun `adaptive engine lifts shadows for dark or underexposed photos`() {
        val nightPreset = PresetLibrary.getById("night")
        assertNotNull(nightPreset)

        val darkAnalysis = ImageAnalysisResult.DEFAULT.copy(
            meanLuminance = 0.20f,
            p10Shadows = 0.04f,
            shadowClippingRisk = 0.15f,
            dominantTone = ImageAnalysisResult.DominantTone.LOW_LIGHT_NIGHT
        )

        val adjustments = AdaptiveEngine.computeAdjustments(darkAnalysis, nightPreset)
        assertTrue(
            "Shadow rescue must be positive",
            adjustments.shadowRescue > 0f
        )
    }

    @Test
    fun `edit parameters reset behavior works correctly`() {
        val edited = EditParameters(
            presetId = "golden_hour",
            presetIntensity = 0.8f,
            manualExposure = 0.2f,
            manualContrast = -0.1f,
            manualSaturation = 0.3f
        )

        assertFalse(edited.isOriginal)
        assertTrue(edited.hasManualAdjustments)

        val resetManual = edited.resetManual()
        assertFalse(resetManual.hasManualAdjustments)
        assertEquals("golden_hour", resetManual.presetId)

        val resetAll = edited.resetAll()
        assertTrue(resetAll.isOriginal)
        assertFalse(resetAll.hasManualAdjustments)
    }

    @Test
    fun `filter renderer renders bitmap deterministically`() {
        val testBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        testBitmap.eraseColor(Color.rgb(180, 140, 100))

        val analysis = ImageAnalyzer.analyze(testBitmap)
        val params = EditParameters(presetId = "soft_film", presetIntensity = 1.0f)

        val rendered1 = FilterRenderer.render(testBitmap, params, analysis)
        val rendered2 = FilterRenderer.render(testBitmap, params, analysis)

        assertNotNull(rendered1)
        assertNotNull(rendered2)
        assertEquals(64, rendered1.width)
        assertEquals(64, rendered1.height)

        // Pixel comparison to confirm deterministic output
        val pixel1 = rendered1.getPixel(32, 32)
        val pixel2 = rendered2.getPixel(32, 32)
        assertEquals("Rendering must be deterministic", pixel1, pixel2)
    }
}
