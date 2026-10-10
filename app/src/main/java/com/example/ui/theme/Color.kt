package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Obsidian & Dark Neutral Base
val ObsidianBackground = Color(0xFF0D0E12)
val CharcoalSurface = Color(0xFF15171F)
val CharcoalSurfaceVariant = Color(0xFF1E212B)
val CharcoalBorder = Color(0xFF2C303E)

// Text & Accents
val TextPrimary = Color(0xFFF2F4F8)
val TextSecondary = Color(0xFFA2A7B8)
val TextMuted = Color(0xFF6B7280)

// Flect Accent Gradient: Lavender to Electric Sky Blue
val LavenderAccent = Color(0xFFB4A5FF)
val ElectricBlueAccent = Color(0xFF5BA7FF)
val MintHighlight = Color(0xFF4EE3A5)
val AmberWarmth = Color(0xFFFFB562)

val FlectBrandGradient = Brush.horizontalGradient(
    colors = listOf(LavenderAccent, ElectricBlueAccent)
)

val FlectSubtleGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1A1C26), Color(0xFF111218))
)
