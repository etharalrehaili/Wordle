package com.wordle.game.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.navigation.GameTopBar

@Composable
fun LeaderboardScreen(onClose: Action) {

    GameTopBar(
        endIcon = Icons.Filled.Close,
        onEndIconClicked = { onClose() },
        modifier = Modifier.fillMaxWidth()
    )
}