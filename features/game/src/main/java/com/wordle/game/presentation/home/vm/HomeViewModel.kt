package com.wordle.game.presentation.home.vm

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wordle.authentication.domain.usecase.GetAuthStateUseCase
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.core.util.Resource
import com.wordle.game.domain.usecases.challenge.GetChallengeSolvedStateUseCase
import com.wordle.game.domain.usecases.profile.CreateProfileUseCase
import com.wordle.game.domain.usecases.profile.GetProfileUseCase
import com.wordle.game.presentation.home.contract.HomeEffect
import com.wordle.game.presentation.home.contract.HomeIntent
import com.wordle.game.presentation.home.contract.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAuthState : GetAuthStateUseCase,
    getChallengeSolvedState: GetChallengeSolvedStateUseCase,
    private val getProfileUseCase   : GetProfileUseCase,
    private val createProfileUseCase: CreateProfileUseCase,
) : BaseMviViewModel<HomeIntent, HomeUiState, HomeEffect>(
    initialState = HomeUiState()
) {

    init {
        viewModelScope.launch {
            getAuthState().collect { isLoggedIn ->
                setState { copy(isLoggedIn = isLoggedIn) }
                if (isLoggedIn) ensureProfileExists()
            }
        }
        viewModelScope.launch {
            // check both languages, show countdown if either is solved
            getChallengeSolvedState("en").combine(
                getChallengeSolvedState("ar")
            ) { enSolved, arSolved ->
                enSolved || arSolved
            }.collect { solved ->
                setState { copy(hasSolvedChallenge = solved) }
            }
        }
    }

    private fun ensureProfileExists() {
        viewModelScope.launch {
            val user  = FirebaseAuth.getInstance().currentUser ?: return@launch
            val uid   = user.uid
            val email = user.email ?: uid

            // Only create if no profile exists yet
            when (val result = getProfileUseCase(uid)) {
                is Resource.Success -> {
                    if (result.data == null) {
                        createProfileUseCase(uid, email.substringBefore("@"))
                    }
                }
                is Resource.Error -> {
                    // Profile fetch failed — attempt creation anyway as a fallback
                    createProfileUseCase(uid, email.substringBefore("@"))
                }
                else -> Unit
            }
        }
    }

    override fun onEvent(intent: HomeIntent) = Unit
}