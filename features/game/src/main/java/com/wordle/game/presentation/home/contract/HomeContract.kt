package com.wordle.game.presentation.home.contract

import com.wordle.core.mvi.UiEffect
import com.wordle.core.mvi.UiIntent
import com.wordle.core.mvi.UiState

data class HomeUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
) : UiState

sealed interface HomeEffect : UiEffect

sealed class HomeIntent : UiIntent