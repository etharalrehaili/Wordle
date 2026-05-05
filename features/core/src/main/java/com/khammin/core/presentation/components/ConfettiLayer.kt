package com.khammin.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ConfettiLayer(modifier: Modifier = Modifier) {
    val confettiColors = listOf(
        colors.logoBlue,
        colors.logoGreen,
        colors.logoPink,
        colors.logoOrange,
        colors.logoTeal,
        colors.logoPurple,
    )

    val particles = remember {
        List(60) {
            ConfettiParticle(
                x      = Random.nextFloat(),
                y      = Random.nextFloat() * -1f,
                color  = confettiColors.random(),
                radius = Random.nextFloat() * 5f + 3f,
                speed  = Random.nextFloat() * 2f + 1.5f,
                angle  = Random.nextFloat() * 360f,
                tilt   = Random.nextFloat() * 10f - 5f,
            )
        }
    }

    val time by produceState(0f) {
        while (true) {
            withFrameMillis { value = it / 1000f }
        }
    }

    Box(modifier = modifier.drawBehind {
        particles.forEach { p ->
            val x     = (p.x + sin(time * p.speed * 0.3f + p.tilt) * 0.05f) * size.width
            val y     = ((p.y + time * p.speed * 0.08f) % 1.2f) * size.height
            drawCircle(
                color  = p.color.copy(alpha = 0.85f),
                radius = p.radius,
                center = Offset(x, y)
            )
        }
    })
}

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val radius: Float,
    val speed: Float,
    val angle: Float,
    val tilt: Float,
)