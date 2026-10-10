package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FlectDarkColorScheme = darkColorScheme(
    primary = LavenderAccent,
    onPrimary = ObsidianBackground,
    primaryContainer = CharcoalSurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = ElectricBlueAccent,
    onSecondary = ObsidianBackground,
    secondaryContainer = CharcoalSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    background = ObsidianBackground,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CharcoalBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Flect is dark-first by design
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = ObsidianBackground.toArgb()
                window.navigationBarColor = ObsidianBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = FlectDarkColorScheme,
        typography = Typography,
        content = content
    )
}
