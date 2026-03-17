package com.wordle.game.presentation.home.vm

import androidx.lifecycle.viewModelScope
import com.wordle.authentication.domain.usecase.GetAuthStateUseCase
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.game.domain.usecases.challenge.GetChallengeSolvedStateUseCase
import com.wordle.game.presentation.home.contract.HomeEffect
import com.wordle.game.presentation.home.contract.HomeIntent
import com.wordle.game.presentation.home.contract.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAuthState          : GetAuthStateUseCase,
    getChallengeSolvedState: GetChallengeSolvedStateUseCase,
) : BaseMviViewModel<HomeIntent, HomeUiState, HomeEffect>(
    initialState = HomeUiState()
) {
    init {
        viewModelScope.launch {
            getAuthState().collect { isLoggedIn ->
                setState { copy(isLoggedIn = isLoggedIn) }
            }
        }
        viewModelScope.launch {
            getChallengeSolvedState().collect { solved ->
                setState { copy(hasSolvedChallenge = solved) }
            }
        }
    }

    override fun onEvent(intent: HomeIntent) = Unit
}