package com.wordle.game.presentation.leaderboard.contract

import com.wordle.core.mvi.UiEffect
import com.wordle.core.mvi.UiIntent
import com.wordle.core.mvi.UiState
import com.wordle.core.presentation.components.enums.LeaderboardFilter
import com.wordle.game.domain.model.Profile

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val players: List<Profile> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedFilter: LeaderboardFilter = LeaderboardFilter.ALL_TIME,
    val language: String = "en",
) : UiState

sealed interface LeaderboardEffect : UiEffect

sealed class LeaderboardIntent : UiIntent {
    data object Refresh : LeaderboardIntent()
    data class ChangeFilter(val filter: LeaderboardFilter) : LeaderboardIntent()
    data class ChangeLanguage(val language: String) : LeaderboardIntent()
}