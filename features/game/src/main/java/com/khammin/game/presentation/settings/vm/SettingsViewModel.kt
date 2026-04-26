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
        // Load the current user's email as soon as the ViewModel is created
        loadEmail()
    }

    /**
     * Fetches the currently logged-in user's email from Firebase
     * and stores it in the UI state for display in the settings screen.
     */
    private fun loadEmail() {
        val user    = FirebaseAuth.getInstance().currentUser
        val email   = user?.email ?: ""
        val isGuest = user == null || user.isAnonymous
        setState { copy(email = email, isGuest = isGuest) }
    }

    override fun onEvent(intent: SettingsIntent) {
        when (intent) {

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