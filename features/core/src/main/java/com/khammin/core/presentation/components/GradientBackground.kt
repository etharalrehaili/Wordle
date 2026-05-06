package com.khammin.core.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.khammin.core.presentation.theme.GameDesignTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb

@Composable
fun GradientBackground(modifier: Modifier = Modifier) {
    val colors = GameDesignTheme.colors

    val infiniteA = rememberInfiniteTransition(label = "floatA")
    val infiniteB = rememberInfiniteTransition(label = "floatB")

    val offsetA by infiniteA.animateFloat(
        initialValue  = 0f,
        targetValue   = -60f,
        animationSpec = infiniteRepeatable(
            animation  = tween(7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "offsetA"
    )

    val offsetB by infiniteB.animateFloat(
        initialValue  = 0f,
        targetValue   = 50f,
        animationSpec = infiniteRepeatable(
            animation  = tween(10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "offsetB"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawIntoCanvas { canvas ->

            // Orb 1 — top left — purple
            canvas.drawCircle(
                center = androidx.compose.ui.geometry.Offset(
                    x = w * 0.05f,
                    y = h * 0.05f + offsetA
                ),
                radius = w * 0.45f,
                paint  = blurPaint(colors.gradientPrimary, w * 0.45f)
            )

            // Orb 2 — right middle — pink
            canvas.drawCircle(
                center = androidx.compose.ui.geometry.Offset(
                    x = w * 0.95f,
                    y = h * 0.45f + offsetB
                ),
                radius = w * 0.40f,
                paint  = blurPaint(colors.gradientSecondary, w * 0.40f)
            )

            // Orb 3 — bottom center — indigo
            canvas.drawCircle(
                center = androidx.compose.ui.geometry.Offset(
                    x = w * 0.40f,
                    y = h * 0.85f + offsetA * -1f
                ),
                radius = w * 0.38f,
                paint  = blurPaint(colors.gradientAccent, w * 0.38f)
            )

            // Orb 4 — left bottom — purple soft
            canvas.drawCircle(
                center = androidx.compose.ui.geometry.Offset(
                    x = w * 0.05f,
                    y = h * 0.70f + offsetB * -1f
                ),
                radius = w * 0.28f,
                paint  = blurPaint(colors.gradientPrimary.copy(alpha = colors.gradientPrimary.alpha * 0.6f), w * 0.28f)
            )
        }
    }
}

private fun blurPaint(color: Color, radius: Float): Paint {
    return Paint().apply {
        asFrameworkPaint().apply {
            isAntiAlias = true
            this.color  = android.graphics.Color.TRANSPARENT
            setShadowLayer(radius * 0.9f, 0f, 0f, color.toArgb())
        }
    }
}