package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FlectLogoMark
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        delay(700)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .clickable { onSplashComplete() }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Abstract gradient luminous wave near the bottom
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val wavePath = Path().apply {
                moveTo(0f, h * 0.72f)
                cubicTo(
                    w * 0.35f, h * 0.65f,
                    w * 0.65f, h * 0.82f,
                    w, h * 0.74f
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }

            drawPath(
                path = wavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AccentViolet.copy(alpha = 0.28f),
                        AccentBlue.copy(alpha = 0.15f),
                        AccentPeach.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    startY = h * 0.65f,
                    endY = h
                )
            )
        }

        // Centered Brand Content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(alphaAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FlectLogoMark(size = 96.dp)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Flect",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "One tap. Your look.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = TextSecondary,
                    letterSpacing = 0.2.sp
                )
            )
        }
    }
}
