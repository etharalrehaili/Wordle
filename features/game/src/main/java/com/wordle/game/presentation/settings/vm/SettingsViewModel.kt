package com.wordle.game.presentation.settings.vm

import com.google.firebase.auth.FirebaseAuth
import com.wordle.authentication.domain.usecase.SignOutUseCase
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.game.presentation.settings.contract.SettingsEffect
import com.wordle.game.presentation.settings.contract.SettingsIntent
import com.wordle.game.presentation.settings.contract.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase,
) : BaseMviViewModel<SettingsIntent, SettingsUiState, SettingsEffect>(
    initialState = SettingsUiState()
) {

    init {
        // Load the current user's email as soon as the ViewModel is created
        loadEmail()
    }

    /**
     * Fetches the currently logged-in user's email from Firebase
     * and stores it in the UI state for display in the settings screen.
     */
    private fun loadEmail() {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        setState { copy(email = email) }
    }

    override fun onEvent(intent: SettingsIntent) {
        when (intent) {

            // Trigger navigation to the change email screen
            SettingsIntent.OnChangeEmailClick ->
                sendEffect { SettingsEffect.NavigateToChangeEmail }

            // Trigger navigation to the change password screen
            SettingsIntent.OnChangePasswordClick ->
                sendEffect { SettingsEffect.NavigateToChangePassword }

            // Sign the user out via Firebase and notify the UI to navigate away
            SettingsIntent.OnSignOutClick -> {
                signOutUseCase()
                sendEffect { SettingsEffect.SignOutSuccess }
            }
        }
    }
}