package com.khammin.game.presentation.home.contract

import com.khammin.core.mvi.UiEffect
import com.khammin.core.mvi.UiIntent
import com.khammin.core.mvi.UiState

data class HomeUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val hasSolvedChallenge: Boolean = false,
) : UiState

sealed interface HomeEffect : UiEffect

sealed class HomeIntent : UiIntent