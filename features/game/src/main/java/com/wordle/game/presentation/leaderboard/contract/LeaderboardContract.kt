package com.wordle.game.presentation.leaderboard.contract

import com.wordle.core.mvi.UiEffect
import com.wordle.core.mvi.UiIntent
import com.wordle.core.mvi.UiState
import com.wordle.game.data.remote.model.ProfileItem

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val players: List<ProfileItem> = emptyList(),
    val error: String? = null,
) : UiState

sealed interface LeaderboardEffect : UiEffect

sealed class LeaderboardIntent : UiIntent {
    data object Refresh : LeaderboardIntent()
}