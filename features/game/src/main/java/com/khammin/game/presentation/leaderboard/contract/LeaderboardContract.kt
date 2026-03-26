package com.khammin.game.presentation.leaderboard.contract

import com.khammin.core.mvi.UiEffect
import com.khammin.core.mvi.UiIntent
import com.khammin.core.mvi.UiState
import com.khammin.core.presentation.components.enums.LeaderboardFilter
import com.khammin.game.domain.model.Profile

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