package com.khammin.core.presentation.components.buttons

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.preview.GameDarkBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme
import com.khammin.core.presentation.theme.GameDesignTheme.colors

@Composable
fun SegmentedButton(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                val backgroundColor by animateColorAsState(
                    targetValue   = if (isSelected) colors.logoBlue.copy(alpha = 0.15f) else Color.Transparent,
                    animationSpec = tween(durationMillis = 250),
                    label         = "segmentBg"
                )
                val textColor by animateColorAsState(
                    targetValue   = if (isSelected) colors.logoBlue else colors.body.copy(alpha = 0.45f),
                    animationSpec = tween(durationMillis = 250),
                    label         = "segmentText"
                )
                val borderColor by animateColorAsState(
                    targetValue   = if (isSelected) colors.logoBlue.copy(alpha = 0.40f) else Color.Transparent,
                    animationSpec = tween(durationMillis = 250),
                    label         = "segmentBorder"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .border(
                            width = 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = { onOptionSelected(option) }
                        )
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = option,
                        fontSize   = GameDesignTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color      = textColor,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        textAlign  = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@GameDarkBackgroundPreview
@Composable
fun SegmentedButtonPreview() {
    var selectedOption1 by remember { mutableStateOf("This Week") }
    var selectedOption2 by remember { mutableStateOf("This Month") }
    var selectedAuthOption by remember { mutableStateOf("Sign Up") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Default style
        SegmentedButton(
            options = listOf("All Time", "This Week", "This Month"),
            selectedOption = selectedOption1,
            onOptionSelected = { selectedOption1 = it }
        )

        // Different selection
        SegmentedButton(
            options = listOf("All Time", "This Week", "This Month"),
            selectedOption = selectedOption2,
            onOptionSelected = { selectedOption2 = it }
        )

        // Custom colors
        SegmentedButton(
            options = listOf("All Time", "This Week", "This Month"),
            selectedOption = "This Week",
            onOptionSelected = { },
        )

        // Auth toggle (2 options)
        SegmentedButton(
            options = listOf("Login", "Sign Up"),
            selectedOption = selectedAuthOption,
            onOptionSelected = { selectedAuthOption = it },
        )
    }
}