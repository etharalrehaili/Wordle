package com.wordle.authentication.presentation.contract

import com.wordle.core.mvi.UiEffect
import com.wordle.core.mvi.UiIntent
import com.wordle.core.mvi.UiState

data class AuthUiState(
    val name: String            = "",
    val email: String           = "",
    val password: String        = "",
    val confirmPassword: String = "",
    val isLoading: Boolean      = false,
    val emailError: String?           = null,
    val passwordError: String?        = null,
    val confirmPasswordError: String? = null,
    val nameError: String?            = null,
) : UiState

sealed interface AuthIntent : UiIntent {
    data class OnNameChanged(val name: String)                   : AuthIntent
    data class OnEmailChanged(val email: String)                 : AuthIntent
    data class OnPasswordChanged(val password: String)           : AuthIntent
    data class OnConfirmPasswordChanged(val password: String)    : AuthIntent
    data object OnLoginClick                                     : AuthIntent
    data object OnSignUpClick                                    : AuthIntent
}

sealed interface AuthEffect : UiEffect {
    data object NavigateToHome        : AuthEffect
    data object SignUpSuccess         : AuthEffect
    data class ShowError(val message: String) : AuthEffect
}