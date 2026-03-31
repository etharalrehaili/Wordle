package com.khammin.game.presentation.home.contract

import com.khammin.core.mvi.UiEffect
import com.khammin.core.mvi.UiIntent
import com.khammin.core.mvi.UiState

data class HomeUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val hasSolvedChallenge: Boolean = false,
    val joinRoomLoading: Boolean = false,
    val joinRoomError: String? = null,
    val createRoomLoading: Boolean = false,
    val noInternetError: Boolean = false
) : UiState

sealed interface HomeEffect : UiEffect

sealed class HomeIntent : UiIntent