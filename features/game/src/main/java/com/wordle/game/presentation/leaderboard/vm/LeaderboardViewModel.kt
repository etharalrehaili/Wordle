package com.wordle.game.presentation.leaderboard.vm

import androidx.lifecycle.viewModelScope
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.core.util.Resource
import com.wordle.game.domain.usecases.leaderboard.GetLeaderboardUseCase
import com.wordle.game.presentation.leaderboard.contract.LeaderboardEffect
import com.wordle.game.presentation.leaderboard.contract.LeaderboardIntent
import com.wordle.game.presentation.leaderboard.contract.LeaderboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
) : BaseMviViewModel<LeaderboardIntent, LeaderboardUiState, LeaderboardEffect>(
    initialState = LeaderboardUiState()
) {
    init { loadLeaderboard() }

    override fun onEvent(intent: LeaderboardIntent) {
        when (intent) {
            LeaderboardIntent.Refresh -> loadLeaderboard()
            is LeaderboardIntent.ChangeFilter -> {
                setState { copy(selectedFilter = intent.filter) }
            }
        }
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            when (val result = getLeaderboardUseCase(limit = 10)) {
                is Resource.Success -> setState {
                    copy(
                        isLoading = false,
                        players = result.data.filter { it.wordsSolved >= 1 }
                    )
                }
                is Resource.Error   -> setState { copy(isLoading = false, error = result.message) }
                else -> Unit
            }
        }
    }
}