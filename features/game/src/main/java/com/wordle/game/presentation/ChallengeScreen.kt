package com.wordle.game.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.bottomsheets.WordleInfoBottomSheet
import com.wordle.core.presentation.components.navigation.GameTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(onClose: Action) {

    var showInfoSheet by remember { mutableStateOf(false) }
    val infoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    GameTopBar(
        endIcon = Icons.Filled.Close,
        startIcon = Icons.Filled.Info,
        onEndIconClicked = { onClose() },
        onStartIconClicked = { showInfoSheet = true },
        modifier = Modifier.fillMaxWidth()
    )

    if (showInfoSheet) {
        WordleInfoBottomSheet(
            sheetState = infoSheetState,
            onDismiss = { showInfoSheet = false }
        )
    }
}