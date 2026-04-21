package com.khammin.game.presentation.game.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.game.presentation.game.vm.MultiplayerGameViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MultiplayerGameScreen(
    onClose: Action,
    currentLanguage: AppLanguage,
    roomId: String = "",
    isHost: Boolean = false,
    userId: String = "",
    isCustomWord: Boolean = false,
    isLobbyMode: Boolean = false,
    viewModel: MultiplayerGameViewModel = hiltViewModel()
) {
    if (isCustomWord) {
        CustomWordGameScreen(
            onClose         = onClose,
            currentLanguage = currentLanguage,
            roomId          = roomId,
            isHost          = isHost,
            userId          = userId,
            viewModel       = viewModel,
        )
    } else {
        RandomWordGameScreen(
            onClose         = onClose,
            currentLanguage = currentLanguage,
            roomId          = roomId,
            isHost          = isHost,
            userId          = userId,
            isLobbyMode     = isLobbyMode,
            viewModel       = viewModel,
        )
    }
}
