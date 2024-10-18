package com.gammaplay.findmyphone.presentation.overlay

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RippleEffectOverlayScreen(
    onStopClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFCE4EC),  // Light Pinkish
                        Color(0xFFF8BBD0)   // Deeper pink at the bottom
                    )
                )
            )
            .pointerInput(Unit) { // Captures touch events to prevent interactions below the overlay
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent() // Consumes all touch events
                    }
                }
            }, // This ensures no touches go through the overlay
        contentAlignment = Alignment.Center
    ) {
        // Your ripple effect animation
        val infiniteTransition = rememberInfiniteTransition()

        // Ripple animation for circles
        val rippleSize1 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = ""
        )
        val rippleSize2 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = ""
        )
        val rippleSize3 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = ""
        )

        // Ripple layers
        RippleCircle(sizeFraction = rippleSize1, color = Color.Red.copy(alpha = 0.2f))
        RippleCircle(sizeFraction = rippleSize2, color = Color.Red.copy(alpha = 0.15f))
        RippleCircle(sizeFraction = rippleSize3, color = Color.Red.copy(alpha = 0.1f))

        // Central stop button
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(Color.Red)
                .clickable { onStopClick() }, // Handles click only on this button
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Stop",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RippleCircle(sizeFraction: Float, color: Color) {
    Box(
        modifier = Modifier
            .size(300.dp * sizeFraction)
            .clip(CircleShape)
            .background(color)
    )
}