package com.khammin.core.presentation.components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.alias.Action

@Composable
fun GameButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector? = null,
    backgroundColor: Color? = null,
    contentColor: Color = Color.White,
    showBorder: Boolean = true,
    borderColor: Color? = null,
    onClick: Action,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "buttonScale"
    )

    val defaultBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF1A2535), Color(0xFF0F1923))
    )
    val bgModifier = if (backgroundColor != null)
        Modifier.background(backgroundColor, RoundedCornerShape(16.dp))
    else
        Modifier.background(brush = defaultBrush, shape = RoundedCornerShape(16.dp))

    val resolvedBorderColor = borderColor
        ?: if (isPressed) Color(0xFF4A6080) else Color(0xFF2A3A50)

    val borderModifier = if (showBorder)
        Modifier.border(
            width = 1.dp,
            color = resolvedBorderColor,
            shape = RoundedCornerShape(28.dp)
        )
    else
        Modifier

    Box(
        modifier = modifier
            .scale(scale)
            .widthIn(min = 280.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(28.dp))
            .then(bgModifier)
            .then(borderModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = label,
                color = contentColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1520)
@Composable
fun GameButtonPreview() {
    Box(modifier = Modifier.padding(24.dp)) {
        GameButton(
            label = "Leaderboard",
            icon = Icons.Default.BarChart,
            contentColor = Color.White,
            backgroundColor = Color(0xFF2A3A50),
            onClick = {}
        )
    }
}