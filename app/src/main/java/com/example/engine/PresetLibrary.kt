package com.example.engine

import com.example.model.AdaptiveLimits
import com.example.model.FilterPreset
import com.example.model.PresetCategory

object PresetLibrary {

    val allPresets: List<FilterPreset> = listOf(
        FilterPreset(
            id = "golden_hour",
            name = "Golden Hour",
            tagline = "Warm highlights, gentle contrast & restrained warmth",
            category = PresetCategory.ESSENTIALS,
            version = 1,
            defaultIntensity = 1.0f,
            exposure = 0.06f,
            contrast = 0.08f,
            saturation = 0.08f,
            warmth = 0.22f,
            tint = 0.02f,
            highlights = 0.08f,
            shadows = 0.06f,
            rMix = 1.08f,
            bMix = 0.94f,
            adaptiveLimits = AdaptiveLimits(
                maxWarmthOffset = 0.25f,
                maxExposureOffset = 0.12f
            )
        ),
        FilterPreset(
            id = "soft_film",
            name = "Soft Film",
            tagline = "Muted colors, softer contrast & subtle film finish",
            category = PresetCategory.FILM,
            version = 1,
            defaultIntensity = 1.0f,
            exposure = 0.02f,
            contrast = -0.10f,
            saturation = -0.14f,
            warmth = 0.05f,
            blackPointLift = 0.08f,
            grain = 0.12f,
            shadows = 0.08f,
            adaptiveLimits = AdaptiveLimits(
                maxContrastOffset = 0.12f
            )
        ),
        FilterPreset(
            id = "cinemood",
            name = "CineMood",
            tagline = "Cinematic tonal separation & controlled shadows",
            category = PresetCategory.CINEMATIC,
            version = 1,
            defaultIntensity = 1.0f,
            contrast = 0.18f,
            saturation = -0.08f,
            warmth = 0.04f,
            rMix = 0.96f,
            gMix = 1.02f,
            bMix = 1.08f,
            shadows = -0.06f,
            vignette = 0.16f,
            adaptiveLimits = AdaptiveLimits(
                shadowRescueLimit = 0.20f
            )
        ),
        FilterPreset(
            id = "retro",
            name = "Retro",
            tagline = "Nostalgic color treatment & warm faded blacks",
            category = PresetCategory.FILM,
            version = 1,
            defaultIntensity = 1.0f,
            warmth = 0.18f,
            tint = -0.04f,
            contrast = -0.06f,
            saturation = -0.08f,
            blackPointLift = 0.12f,
            grain = 0.15f,
            vignette = 0.12f,
            rMix = 1.06f,
            bMix = 0.92f
        ),
        FilterPreset(
            id = "cool_slate",
            name = "Cool Slate",
            tagline = "Cooler tonal character with neutral balance",
            category = PresetCategory.MOOD,
            version = 1,
            defaultIntensity = 1.0f,
            warmth = -0.22f,
            tint = 0.02f,
            contrast = 0.10f,
            saturation = -0.06f,
            rMix = 0.94f,
            bMix = 1.08f,
            adaptiveLimits = AdaptiveLimits(
                maxWarmthOffset = 0.20f
            )
        ),
        FilterPreset(
            id = "clean",
            name = "Clean",
            tagline = "Natural color, balanced exposure & gentle punch",
            category = PresetCategory.ESSENTIALS,
            version = 1,
            defaultIntensity = 1.0f,
            exposure = 0.04f,
            contrast = 0.08f,
            saturation = 0.06f,
            highlights = -0.04f,
            shadows = 0.06f
        ),
        FilterPreset(
            id = "matte",
            name = "Matte",
            tagline = "Lifted blacks & smooth velvet contrast",
            category = PresetCategory.FILM,
            version = 1,
            defaultIntensity = 1.0f,
            contrast = -0.14f,
            saturation = -0.12f,
            blackPointLift = 0.18f,
            shadows = 0.12f,
            grain = 0.08f
        ),
        FilterPreset(
            id = "vivid",
            name = "Vivid",
            tagline = "Expressive colors with highlight protection",
            category = PresetCategory.ESSENTIALS,
            version = 1,
            defaultIntensity = 1.0f,
            exposure = 0.02f,
            contrast = 0.14f,
            saturation = 0.28f,
            highlights = -0.10f, // clipping protection
            shadows = 0.04f,
            adaptiveLimits = AdaptiveLimits(
                highlightProtectionLimit = 0.35f
            )
        ),
        FilterPreset(
            id = "night",
            name = "Night",
            tagline = "Controlled tonal separation for darker photos",
            category = PresetCategory.MOOD,
            version = 1,
            defaultIntensity = 1.0f,
            exposure = 0.08f,
            contrast = 0.10f,
            shadows = 0.16f,
            highlights = -0.12f,
            blackPointLift = 0.04f,
            saturation = 0.04f,
            vignette = 0.12f,
            adaptiveLimits = AdaptiveLimits(
                shadowRescueLimit = 0.30f
            )
        ),
        FilterPreset(
            id = "mono",
            name = "Mono",
            tagline = "Black-and-white conversion with deep contrast",
            category = PresetCategory.ESSENTIALS,
            version = 1,
            defaultIntensity = 1.0f,
            contrast = 0.18f,
            shadows = 0.04f,
            highlights = -0.04f,
            grain = 0.08f,
            isMonochrome = true
        ),
        FilterPreset(
            id = "portrait",
            name = "Portrait",
            tagline = "Gentle skin tones, soft highlights & flattering warmth",
            category = PresetCategory.ESSENTIALS,
            version = 1,
            defaultIntensity = 1.0f,
            warmth = 0.06f,
            tint = 0.03f,
            contrast = 0.04f,
            highlights = -0.08f, // reduce harsh skin shine
            shadows = 0.08f,     // open up eye/facial shadows
            saturation = 0.04f,
            adaptiveLimits = AdaptiveLimits(
                maxWarmthOffset = 0.18f
            )
        ),
        FilterPreset(
            id = "travel",
            name = "Travel",
            tagline = "Balanced outdoor & landscape enhancement",
            category = PresetCategory.CINEMATIC,
            version = 1,
            defaultIntensity = 1.0f,
            exposure = 0.04f,
            contrast = 0.14f,
            saturation = 0.16f,
            highlights = -0.06f,
            shadows = 0.08f,
            rMix = 1.02f,
            gMix = 1.04f,
            bMix = 1.02f
        )
    )

    fun getById(id: String?): FilterPreset? = allPresets.find { it.id == id }

    fun defaultPreset(): FilterPreset = allPresets.first()
}
