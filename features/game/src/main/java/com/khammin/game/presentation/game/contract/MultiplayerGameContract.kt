package com.khammin.game.presentation.game.contract

import com.khammin.core.domain.model.PlayerState
import com.khammin.core.mvi.UiEffect
import com.khammin.core.mvi.UiIntent
import com.khammin.core.mvi.UiState
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.enums.TileState

data class MultiplayerGameUiState(
    val wordLength: Int = 4,
    val currentRow: Int = 0,
    val currentCol: Int = 0,
    val board: List<List<Tile>> = List(MAX_GUESSES) { List(4) { Tile() } },
    val keyboardStates: Map<Char, TileState> = emptyMap(),
    val targetWord: String = "",
    val roomId: String = "",
    val myUserId: String = "",
    val opponentId: String = "",
    val opponentName: String = "Guest",
    val opponentLeft: Boolean = false,
    val opponentFailed: Boolean = false,
    val myName: String = "You",
    val opponentAvatarUrl: String? = null,
    val opponentState: PlayerState? = null,
    val isLoading: Boolean = false,
    val isOpponentProfileLoading: Boolean = false,
    val error: String? = null,
    val isGameOver: Boolean = false,
    val isHost: Boolean = false,
    val isCustomWord: Boolean = false,
    val language: String = "",
    val defaultMyName: String = "You",
    val defaultGuestName: String = "Guest",
    ) : UiState

sealed interface MultiplayerGameEffect : UiEffect {
    data class ShowGameDialog(val isWin: Boolean, val targetWord: String, val opponentLeft: Boolean = false, val opponentFailed: Boolean = false) : MultiplayerGameEffect
    data object InvalidWord : MultiplayerGameEffect
    data object NotInWordList : MultiplayerGameEffect
    data object RowShake : MultiplayerGameEffect
    data object NavigateBack : MultiplayerGameEffect
    data object DismissResultDialog : MultiplayerGameEffect
    data object OpponentDisconnected : MultiplayerGameEffect
}

sealed class MultiplayerGameIntent : UiIntent {
    data class LoadGame(
        val roomId: String,
        val language: String,
        val isHost: Boolean,
        val myUserId: String = "",
        val isCustomWord: Boolean = false,
        val defaultMyName: String = "You",
        val defaultGuestName: String = "Guest",
    ) : MultiplayerGameIntent()
    data class EnterLetter(val letter: Char) : MultiplayerGameIntent()
    data object DeleteLetter : MultiplayerGameIntent()
    data object SubmitGuess : MultiplayerGameIntent()
    data object RestartGame : MultiplayerGameIntent()
    data object LeaveMatch : MultiplayerGameIntent()
}