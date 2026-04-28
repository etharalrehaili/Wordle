package com.khammin.game.presentation.leaderboard.vm

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.viewModelScope
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.util.NetworkUtils
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
    private val networkUtils: NetworkUtils,
) : BaseMviViewModel<LeaderboardIntent, LeaderboardUiState, LeaderboardEffect>(
    initialState = LeaderboardUiState()
) {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onEvent(intent: LeaderboardIntent) {
        when (intent) {
            LeaderboardIntent.Refresh -> {
                setState { copy(isRefreshing = true) }
                loadLeaderboard(isRefresh = true, language = uiState.value.language)
            }
            LeaderboardIntent.Retry -> {
                setState { copy(isRetrying = true) }
                loadLeaderboard(isRetry = true, language = uiState.value.language)
            }
            is LeaderboardIntent.ChangeFilter -> setState { copy(selectedFilter = intent.filter) }
            is LeaderboardIntent.ChangeLanguage -> {
                setState { copy(language = intent.language) }
                loadLeaderboard(language = intent.language)
            }
            LeaderboardIntent.DismissNoInternet -> setState { copy(noInternet = false) }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun loadLeaderboard(isRefresh: Boolean = false, isRetry: Boolean = false, language: String) {
        viewModelScope.launch {
            val connected = networkUtils.isConnected()

            if (!connected) {
                setState { copy(isLoading = false, isRefreshing = false, isRetrying = false, noInternet = true) }
                return@launch
            }

            if (!isRefresh && !isRetry) setState { copy(isLoading = true, error = null, noInternet = false) }

            when (val result = getLeaderboardUseCase(limit = 100, language = language)) {
                is Resource.Success -> {
                    setState {
                        copy(
                            isLoading    = false,
                            isRefreshing = false,
                            isRetrying   = false,
                            noInternet   = false,
                            players      = result.data.filter { it.arCurrentPoints > 0 }
                        )
                    }
                }
                is Resource.Error -> {
                    val isNoInternet = result.message?.contains("Unable to resolve host") == true ||
                            result.message?.contains("No address associated") == true
                    setState {
                        copy(
                            isLoading    = false,
                            isRefreshing = false,
                            isRetrying   = false,
                            error        = if (isNoInternet) null else result.message,
                            noInternet   = isNoInternet
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}