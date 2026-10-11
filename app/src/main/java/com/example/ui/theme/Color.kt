package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Reference Palette from Flect UI Mockup
val NavyBackground = Color(0xFF080D16)          // Primary deep near-black navy
val NavySecondaryBg = Color(0xFF0D1420)         // Secondary background
val SurfaceElevated = Color(0xFF141D2B)         // Elevated surface
val SurfaceHigher = Color(0xFF1B2636)           // Higher-level surface
val SurfaceGlass = Color(0xD9141D2B)
val BorderSubtle = Color(0xFF1F2B3E)           // Subtle blue-gray divider/border
val BorderActive = Color(0xFF8967FF)           // Active violet border

// Text colors
val TextPrimary = Color(0xFFF5F7FC)            // Primary clean white text
val TextSecondary = Color(0xFFA4B0C3)          // Secondary muted blue-gray text
val TextMuted = Color(0xFF718096)              // Muted caption text

// Accent colors
val AccentViolet = Color(0xFF8967FF)           // Vibrant brand violet
val AccentBlue = Color(0xFF61B9FF)             // Vibrant sky blue
val AccentPeach = Color(0xFFFFB580)            // Warm peach ribbon highlight
val AccentGreen = Color(0xFF4EE3A5)            // Subtle success green

// Gradients
val FlectBrandGradient = Brush.horizontalGradient(
    colors = listOf(AccentViolet, AccentBlue)
)

val FlectLogoGradient = Brush.linearGradient(
    colors = listOf(AccentViolet, AccentBlue, AccentPeach)
)

val FlectWaveGradient = Brush.verticalGradient(
    colors = listOf(Color(0x008967FF), Color(0x338967FF), Color(0x6661B9FF), Color(0x00080D16))
)

// Legacy aliases for backward compatibility across components
val ObsidianBackground = NavyBackground
val ObsidianSurfaceDark = NavySecondaryBg
val CharcoalSurface = SurfaceElevated
val CharcoalSurfaceVariant = SurfaceHigher
val CharcoalBorder = BorderSubtle
val GlassBorder = BorderSubtle
val GlassBorderActive = BorderActive
val GlassSurface = SurfaceGlass
val LavenderAccent = AccentViolet
val ElectricBlueAccent = AccentBlue
val MintHighlight = AccentGreen
val AmberWarmth = AccentPeach
val ChampagneGold = AccentPeach
