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

    init {
        android.util.Log.d("Network", "ViewModel init — isConnected = ${networkUtils.isConnected()}")
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onEvent(intent: LeaderboardIntent) {
        when (intent) {
            LeaderboardIntent.Refresh -> {
                setState { copy(isRefreshing = true) }
                loadLeaderboard(isRefresh = true, language = uiState.value.language)
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
    private fun loadLeaderboard(isRefresh: Boolean = false, language: String) {
        android.util.Log.d("Network", "loadLeaderboard called — checking network first")

        val connected = networkUtils.isConnected()
        android.util.Log.d("Network", "isConnected = $connected")

        if (!connected) {
            android.util.Log.d("Network", "No internet — setting noInternet = true, returning early")
            setState { copy(isLoading = false, isRefreshing = false, noInternet = true) }
            return
        }

        android.util.Log.d("Network", "Has internet — launching coroutine to call API")
        viewModelScope.launch {
            if (!isRefresh) setState { copy(isLoading = true, error = null, noInternet = false) }
            android.util.Log.d("Network", "Calling getLeaderboardUseCase")
            when (val result = getLeaderboardUseCase(limit = 10, language = language)) {
                is Resource.Success -> {
                    android.util.Log.d("Network", "API success: ${result.data.size} players")
                    setState {
                        copy(
                            isLoading    = false,
                            isRefreshing = false,
                            players      = result.data.filter {
                                if (language == "ar") it.arWordsSolved >= 1
                                else it.enWordsSolved >= 1
                            }
                        )
                    }
                }
                is Resource.Error -> {
                    android.util.Log.e("Network", "API error: ${result.message}")
                    val isNoInternet = result.message?.contains("Unable to resolve host") == true ||
                            result.message?.contains("No address associated") == true
                    setState {
                        copy(
                            isLoading    = false,
                            isRefreshing = false,
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