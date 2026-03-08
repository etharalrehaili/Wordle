package com.wordle.game.presentation.viewmodel

import com.wordle.authentication.domain.usecase.SignOutUseCase
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.game.presentation.contract.SettingsEffect
import com.wordle.game.presentation.contract.SettingsIntent
import com.wordle.game.presentation.contract.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase,
) : BaseMviViewModel<SettingsIntent, SettingsUiState, SettingsEffect>(
    initialState = SettingsUiState()
) {

    override fun onEvent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.OnChangeEmailClick    ->
                sendEffect { SettingsEffect.NavigateToChangeEmail }
            SettingsIntent.OnChangePasswordClick ->
                sendEffect { SettingsEffect.NavigateToChangePassword }
            SettingsIntent.OnSignOutClick        -> {
                signOutUseCase()
                sendEffect { SettingsEffect.SignOutSuccess }
            }
        }
    }
}