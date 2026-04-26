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
import com.khammin.core.presentation.theme.GameDesignTheme.colors

enum class GameButtonVariant {
    Primary,  // Quick Play — blue fill
    Muted,    // Challenge — gray fill
    Ghost     // Leaderboard — outlined
}

enum class GameButtonSize {
    Regular,  // default — 64dp height, 18sp
    Small     // compact — 44dp height, 15sp
}

@Composable
fun GameButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector? = null,
    variant: GameButtonVariant = GameButtonVariant.Primary,
    size: GameButtonSize = GameButtonSize.Regular,
    enabled: Boolean = true,
    onClick: Action,
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
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
    val buttonHeight = if (size == GameButtonSize.Small) 44.dp else 64.dp
    val cornerRadius = if (size == GameButtonSize.Small) 14.dp else 28.dp
    val fontSize     = if (size == GameButtonSize.Small) 15.sp else 18.sp
    val minWidth     = if (size == GameButtonSize.Small) 120.dp else 280.dp

    Box(
        modifier = modifier
            .scale(scale)
            .widthIn(min = minWidth)
            .height(buttonHeight)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.38f),
                RoundedCornerShape(cornerRadius)
            )
            .then(
                if (showBorder) Modifier.border(
                    width = 1.5.dp,
                    color = if (enabled) colors.buttonGhostBorder else colors.buttonGhostBorder.copy(alpha = 0.38f),
                    shape = RoundedCornerShape(cornerRadius)
                ) else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val alphaColor = if (enabled) contentColor else contentColor.copy(alpha = 0.38f)
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = alphaColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = label,
                color = alphaColor,
                fontSize = fontSize,
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