package com.khammin.game.presentation.game.contract

import com.khammin.core.domain.model.PlayerState
import com.khammin.core.mvi.UiEffect
import com.khammin.core.mvi.UiIntent
import com.khammin.core.mvi.UiState
import com.khammin.core.presentation.components.GuessRow
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.enums.TileState

data class WaitingPlayer(
    val userId: String,
    val name: String,
    val avatarUrl: String? = null,
    val avatarColor: Long? = null,
    val avatarEmoji: String? = null,
)

data class OpponentProgress(
    val name: String = "",
    val avatarUrl: String? = null,
    val solved: Boolean = false,
    val failed: Boolean = false,
    val guessCount: Int = 0,
    val guessRows: List<GuessRow> = List(MAX_GUESSES) { GuessRow() },
    val totalPoints: Int = 0,
)

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
    val opponentAvatarColor: Long? = null,
    val opponentAvatarEmoji: String? = null,
    val opponentState: PlayerState? = null,
    val isLoading: Boolean = false,
    val isOpponentProfileLoading: Boolean = false,
    val error: String? = null,
    val isGameOver: Boolean = false,
    val isMyWin: Boolean = false,
    val isHost: Boolean = false,
    val isCustomWord: Boolean = false,
    val language: String = "",
    val defaultMyName: String = "You",
    val defaultGuestName: String = "Guest",
    // Multi-player custom word fields
    val roomStatus: String = "waiting",
    val guestIds: List<String> = emptyList(),
    val waitingPlayers: List<WaitingPlayer> = emptyList(),
    val opponentsProgress: Map<String, OpponentProgress> = emptyMap(),
    val isHostLeft: Boolean = false,
    val playAgainVotes: List<String> = emptyList(),
    val roundNumber: Int = 1,
    val totalPoints: Map<String, Int> = emptyMap(),
    // Local avatar (anonymous users only); null = no custom avatar chosen yet
    val avatarColor: Long? = null,
    val avatarEmoji: String? = null,
    val isAnonymous: Boolean = false,
) : UiState

sealed interface MultiplayerGameEffect : UiEffect {
    data class ShowGameDialog(val isWin: Boolean, val targetWord: String, val opponentLeft: Boolean = false, val opponentFailed: Boolean = false, val winnerName: String = "") : MultiplayerGameEffect
    data object InvalidWord : MultiplayerGameEffect
    data object NotInWordList : MultiplayerGameEffect
    data object RowShake : MultiplayerGameEffect
    data object NavigateBack : MultiplayerGameEffect
    data object DismissResultDialog : MultiplayerGameEffect
    data object OpponentDisconnected : MultiplayerGameEffect
    data object HostLeftRoom : MultiplayerGameEffect
    data object AllPlayersLeft : MultiplayerGameEffect
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
    data object StartMatch : MultiplayerGameIntent()
    data class StartMatchWithWord(val word: String) : MultiplayerGameIntent()
    data class PlayAgainCustomWord(val newWord: String) : MultiplayerGameIntent()
    data object VotePlayAgain : MultiplayerGameIntent()
    data class UpdateGuestProfile(
        val name: String,
        val avatarColor: Long?,
        val avatarEmoji: String?,
    ) : MultiplayerGameIntent()
}
