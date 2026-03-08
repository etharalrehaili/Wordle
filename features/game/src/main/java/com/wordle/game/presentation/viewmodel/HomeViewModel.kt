package com.wordle.game.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.wordle.authentication.domain.usecase.GetAuthStateUseCase
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.game.presentation.contract.HomeEffect
import com.wordle.game.presentation.contract.HomeIntent
import com.wordle.game.presentation.contract.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAuthState: GetAuthStateUseCase,
) : BaseMviViewModel<HomeIntent, HomeUiState, HomeEffect>(
    initialState = HomeUiState()
) {
    init {
        viewModelScope.launch {
            getAuthState().collect { isLoggedIn ->
                setState { copy(isLoggedIn = isLoggedIn) }
            }
        }
    }

    override fun onEvent(intent: HomeIntent) = Unit
}