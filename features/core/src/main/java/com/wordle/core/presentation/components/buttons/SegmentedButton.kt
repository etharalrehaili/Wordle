package com.wordle.core.presentation.components.buttons

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.presentation.preview.GameDarkBackgroundPreview
import com.wordle.core.presentation.theme.LocalWordleColors

@Composable
fun SegmentedButton(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalWordleColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.surface,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            color = if (isSelected) colors.key else Color.Transparent
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onOptionSelected(option)
                        }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) colors.title else colors.body
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