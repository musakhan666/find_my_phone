package com.gammaplay.findmyphone.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// Helper functions for drawing shadows

fun DrawScope.drawTopSectionShadow() {
    val size = this.size
    drawContext.canvas.nativeCanvas.apply {
        drawRoundRect(
            0f,
            0f,
            size.width,
            size.height,
            84.dp.toPx(),
            84.dp.toPx(),
            Paint().apply {
                color = Color.Transparent.toArgb()
                setShadowLayer(
                    16.dp.toPx(),
                    0.dp.toPx(),
                    16.dp.toPx(),
                    Color.Blue
                        .copy(
                            alpha = 0.25f, red = 0.35f, green = 0.44f, blue = 0.91f
                        )
                        .toArgb()
                )
            })
    }
}
 fun DrawScope.drawBigCircleShadow(isActivated: Boolean) {
    val size = this.size
    drawContext.canvas.nativeCanvas.apply {
        drawCircle(
            size.width / 2,
            size.height / 2,
            size.width / 2,
            Paint().apply {
                color = Color.Transparent.toArgb()
                setShadowLayer(
                    5.dp.toPx(),
                    0.dp.toPx(),
                    5.dp.toPx(),
                    Color.Blue
                        .copy(
                            alpha = if (isActivated) 0.4f else 0.5f,
                            red = if (isActivated) 0.19f else 0.57f,
                            green = if (isActivated) 0.49f else 0.61f,
                            blue = if (isActivated) 0.99f else 0.78f
                        )
                        .toArgb()
                )
            }
        )
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun setStatusBarColor(color: Color) {
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = color.toArgb()
        }
    }
}

//Set Status bar icons color
@Composable
fun setStatusBarIconsColor(isDark: Boolean) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(
        color = Color.Transparent,
        darkIcons = isDark
    )
}