package com.khammin.game.presentation.home.contract

import com.khammin.core.mvi.UiEffect
import com.khammin.core.mvi.UiIntent
import com.khammin.core.mvi.UiState

data class HomeUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isEmailVerified: Boolean = false,
    val hasSolvedChallenge: Boolean = false,
    val joinRoomLoading: Boolean = false,
    val joinRoomError: String? = null,
    val createRoomLoading: Boolean = false,
    val noInternetError: Boolean = false,
    val easyWordsSolved: Int = 0,
    val classicWordsSolved: Int = 0,
    val showWelcomeSheet: Boolean = false,
    // Sheet visibility — lives in ViewModel so rotation doesn't close open sheets
    val showGameModeSheet: Boolean = false,
    val showLengthSheet: Boolean = false,
    val showMultiplayerSheet: Boolean = false,
    val showWordPickerSheet: Boolean = false,
    val showJoinRoomSheet: Boolean = false,
    // Room creation type ("random" / "custom" / null) — survives rotation
    val createRoomType: String? = null,
    // Text typed in JoinRoomBottomSheet — survives rotation
    val joinRoomCode: String = "",
) : UiState

sealed interface HomeEffect : UiEffect

sealed class HomeIntent : UiIntent {
    data class ShowGameModeSheet(val show: Boolean) : HomeIntent()
    data class ShowLengthSheet(val show: Boolean) : HomeIntent()
    data class ShowMultiplayerSheet(val show: Boolean) : HomeIntent()
    data class ShowWordPickerSheet(val show: Boolean) : HomeIntent()
    data class ShowJoinRoomSheet(val show: Boolean) : HomeIntent()
    data class SetCreateRoomType(val type: String?) : HomeIntent()
    data class SetJoinRoomCode(val code: String) : HomeIntent()
}
