package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentViolet

/**
 * High-fidelity vector rendition of Flect's iconic ribbon 'F' brand mark
 * rendered in vibrant violet-to-sky-blue-to-peach gradient.
 */
@Composable
fun FlectLogoMark(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val stemWidth = w * 0.28f
        val cornerRad = CornerRadius(stemWidth * 0.45f, stemWidth * 0.45f)

        // 1. Vertical main stem (Violet down to Blue)
        val stemPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = w * 0.08f,
                    top = h * 0.08f,
                    right = w * 0.08f + stemWidth,
                    bottom = h * 0.92f,
                    cornerRadius = cornerRad
                )
            )
        }
        drawPath(
            path = stemPath,
            brush = Brush.verticalGradient(
                colors = listOf(AccentViolet, AccentBlue),
                startY = 0f,
                endY = h
            )
        )

        // 2. Top horizontal arm (Sky Blue)
        val topArmPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = w * 0.08f,
                    top = h * 0.08f,
                    right = w * 0.88f,
                    bottom = h * 0.08f + stemWidth,
                    cornerRadius = cornerRad
                )
            )
        }
        drawPath(
            path = topArmPath,
            brush = Brush.horizontalGradient(
                colors = listOf(AccentViolet, AccentBlue),
                startX = w * 0.08f,
                endX = w * 0.88f
            )
        )

        // 3. Middle horizontal fold arm (Violet to warm Peach highlight)
        val middleArmPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = w * 0.12f,
                    top = h * 0.44f,
                    right = w * 0.72f,
                    bottom = h * 0.44f + stemWidth,
                    cornerRadius = cornerRad
                )
            )
        }
        drawPath(
            path = middleArmPath,
            brush = Brush.horizontalGradient(
                colors = listOf(AccentViolet, AccentPeach),
                startX = w * 0.12f,
                endX = w * 0.72f
            )
        )
    }
}
