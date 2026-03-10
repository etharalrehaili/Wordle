package com.wordle.core.presentation.components.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordle.core.presentation.components.buttons.GameButton
import com.wordle.core.presentation.components.text.WordleText
import com.wordle.core.presentation.preview.GameLightBackgroundPreview
import com.wordle.core.presentation.theme.GameDesignTheme
import com.wordle.core.presentation.theme.LocalWordleColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordLengthSelectionBottomSheet(
    onLengthSelected: (Int) -> Unit = {},
    onDismiss: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val colors = LocalWordleColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = null,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WordleText(
                text       = "Choose Word Length",
                color      = colors.title,
                fontSize   = GameDesignTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            listOf(4, 5, 6).forEach { length ->
                GameButton(
                    label           = "$length Letters",
                    backgroundColor = colors.correct,
                    onClick         = { onLengthSelected(length) },
                    modifier        = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@GameLightBackgroundPreview
@Composable
fun WordLengthSelectionBottomSheetPreview() {
    WordLengthSelectionBottomSheet()
}