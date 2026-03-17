package com.wordle.authentication.presentation.contract

import UiText
import com.wordle.core.mvi.UiEffect
import com.wordle.core.mvi.UiIntent
import com.wordle.core.mvi.UiState

data class AuthUiState(
    val email: String           = "",
    val password: String        = "",
    val confirmPassword: String = "",
    val isLoading: Boolean      = false,
    val emailError          : UiText? = null,
    val passwordError       : UiText? = null,
    val confirmPasswordError: UiText? = null,
    val nameError: String?            = null,
) : UiState

sealed interface AuthIntent : UiIntent {
    data class OnEmailChanged(val email: String)                 : AuthIntent
    data class OnPasswordChanged(val password: String)           : AuthIntent
    data class OnConfirmPasswordChanged(val password: String)    : AuthIntent
    data object OnLoginClick                                     : AuthIntent
    data object OnSignUpClick                                    : AuthIntent
}

sealed interface AuthEffect : UiEffect {
    data object NavigateToHome        : AuthEffect
    data object SignUpSuccess         : AuthEffect
    data class ShowError(val message: UiText) : AuthEffect
}