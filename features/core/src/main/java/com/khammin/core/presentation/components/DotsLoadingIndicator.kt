package com.khammin.core.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.khammin.core.presentation.theme.GameDesignTheme.colors

@Composable
fun DotsLoadingIndicator(
    color: Color = colors.buttonTeal,
) {
    val dots = 3
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        modifier              = Modifier.padding(vertical = 10.dp)
    ) {
        repeat(dots) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue    = 0.4f,
                targetValue     = 1f,
                animationSpec   = infiniteRepeatable(
                    animation  = keyframes {
                        durationMillis = 900
                        0.4f  at 0
                        1f    at 300
                        0.4f  at 600
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(index * 150)
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(scale)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}