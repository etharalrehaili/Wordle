package com.khammin.game.presentation.leaderboard.vm

import androidx.lifecycle.viewModelScope
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.util.Resource
import com.khammin.game.domain.usecases.leaderboard.GetLeaderboardUseCase
import com.khammin.game.presentation.leaderboard.contract.LeaderboardEffect
import com.khammin.game.presentation.leaderboard.contract.LeaderboardIntent
import com.khammin.game.presentation.leaderboard.contract.LeaderboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
) : BaseMviViewModel<LeaderboardIntent, LeaderboardUiState, LeaderboardEffect>(
    initialState = LeaderboardUiState()
) {

    override fun onEvent(intent: LeaderboardIntent) {
        when (intent) {
            LeaderboardIntent.Refresh -> {
                setState { copy(isRefreshing = true) }
                loadLeaderboard(isRefresh = true, language = uiState.value.language)
            }
            is LeaderboardIntent.ChangeFilter -> {
                setState { copy(selectedFilter = intent.filter) }
            }
            is LeaderboardIntent.ChangeLanguage -> {
                setState { copy(language = intent.language) }
                loadLeaderboard(language = intent.language)
            }
        }
    }


    private fun loadLeaderboard(isRefresh: Boolean = false, language: String) {
        viewModelScope.launch {
            if (!isRefresh) setState { copy(isLoading = true, error = null) }
            when (val result = getLeaderboardUseCase(limit = 10, language = language)) {
                is Resource.Success -> setState {
                    copy(
                        isLoading    = false,
                        isRefreshing = false,
                        players      = result.data.filter {
                            if (language == "ar") it.arWordsSolved >= 1
                            else it.enWordsSolved >= 1
                        }
                    )
                }
                is Resource.Error -> setState {
                    copy(isLoading = false, isRefreshing = false, error = result.message)
                }
                else -> Unit
            }
        }
    }
}