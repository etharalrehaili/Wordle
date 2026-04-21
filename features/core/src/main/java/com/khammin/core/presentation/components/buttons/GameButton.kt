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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.alias.Action
import com.khammin.core.presentation.theme.GameDesignTheme
import com.khammin.core.presentation.theme.GameDesignTheme.colors

enum class GameButtonVariant {
    Primary,  // Quick Play — blue fill
    Muted,    // Challenge — gray fill
    Ghost     // Leaderboard — outlined
}

@Composable
fun GameButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector? = null,
    variant: GameButtonVariant = GameButtonVariant.Primary,
    onClick: Action,
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "buttonScale"
    )

    val backgroundColor = when (variant) {
        GameButtonVariant.Primary -> colors.buttonPrimaryBg
        GameButtonVariant.Muted   -> colors.buttonMutedBg
        GameButtonVariant.Ghost   -> Color.Transparent
    }

    val contentColor = when (variant) {
        GameButtonVariant.Primary -> colors.buttonPrimaryContent
        GameButtonVariant.Muted   -> colors.buttonMutedContent
        GameButtonVariant.Ghost   -> colors.buttonGhostContent
    }

    val showBorder = variant == GameButtonVariant.Ghost

    Box(
        modifier = modifier
            .scale(scale)
            .widthIn(min = 280.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundColor, RoundedCornerShape(28.dp))
            .then(
                if (showBorder) Modifier.border(
                    width = 1.5.dp,
                    color = colors.buttonGhostBorder,
                    shape = RoundedCornerShape(28.dp)
                ) else Modifier
            )
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

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun GameButtonPreviewDark() {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameButton(label = "Quick Play", variant = GameButtonVariant.Primary, onClick = {})
        GameButton(label = "Challenge", variant = GameButtonVariant.Muted, onClick = {})
        GameButton(label = "Leaderboard", variant = GameButtonVariant.Ghost, onClick = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
fun GameButtonPreviewLight() {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameButton(label = "Quick Play", variant = GameButtonVariant.Primary, onClick = {})
        GameButton(label = "Challenge", variant = GameButtonVariant.Muted, onClick = {})
        GameButton(label = "Leaderboard", variant = GameButtonVariant.Ghost, onClick = {})
    }
}