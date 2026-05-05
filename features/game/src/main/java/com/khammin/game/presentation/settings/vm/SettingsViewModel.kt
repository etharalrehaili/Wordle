package com.khammin.game.presentation.settings.vm

import com.google.firebase.auth.FirebaseAuth
import com.khammin.authentication.domain.usecase.SignOutUseCase
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.game.presentation.settings.contract.SettingsEffect
import com.khammin.game.presentation.settings.contract.SettingsIntent
import com.khammin.game.presentation.settings.contract.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase,
) : BaseMviViewModel<SettingsIntent, SettingsUiState, SettingsEffect>(
    initialState = SettingsUiState()
) {

    init {
        loadEmail()
    }

    private fun loadEmail() {
        val user    = FirebaseAuth.getInstance().currentUser
        val email   = user?.email ?: ""
        val isGuest = user == null || user.isAnonymous
        setState { copy(email = email, isGuest = isGuest) }
    }

    override fun onEvent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.OnSignOutClick -> {
                signOutUseCase()
                sendEffect { SettingsEffect.SignOutSuccess }
            }
        }
    }
}
