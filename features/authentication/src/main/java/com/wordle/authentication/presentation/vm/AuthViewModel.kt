package com.wordle.authentication.presentation.vm

import androidx.lifecycle.viewModelScope
import com.wordle.authentication.domain.usecase.LoginUseCase
import com.wordle.authentication.domain.usecase.SignUpUseCase
import com.wordle.authentication.presentation.contract.AuthEffect
import com.wordle.authentication.presentation.contract.AuthIntent
import com.wordle.authentication.presentation.contract.AuthUiState
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.core.mvi.toNetworkError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signUpUseCase: SignUpUseCase,
) : BaseMviViewModel<AuthIntent, AuthUiState, AuthEffect>(
    initialState = AuthUiState()
) {

    override fun onEvent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.OnEmailChanged -> {
                setState { copy(email = intent.email, emailError = null) }
            }

            is AuthIntent.OnPasswordChanged -> {
                setState { copy(password = intent.password, passwordError = null, confirmPasswordError = null) }
            }

            is AuthIntent.OnConfirmPasswordChanged -> {
                setState { copy(confirmPassword = intent.password, confirmPasswordError = null, passwordError = null,) }
            }

            AuthIntent.OnLoginClick -> {
                if (!validateLogin()) return
                setState { copy(isLoading = true) }
                viewModelScope.launch {
                    loginUseCase(
                        email    = uiState.value.email,
                        password = uiState.value.password,
                    ).fold(
                        onSuccess = {
                            setState { copy(isLoading = false) }
                            sendEffect { AuthEffect.NavigateToHome }
                        },
                        onFailure = { error ->
                            setState { copy(isLoading = false) }
                            sendNetworkError(error.toNetworkError()) { errorMessage ->
                                sendEffect { AuthEffect.ShowError(errorMessage) }
                            }
                        }
                    )
                }
            }

            AuthIntent.OnSignUpClick -> {
                if (!validateSignUp()) return
                setState { copy(isLoading = true) }
                viewModelScope.launch {
                    signUpUseCase(
                        email    = uiState.value.email,
                        password = uiState.value.password,
                    ).fold(
                        onSuccess = {
                            setState { copy(isLoading = false) }
                            sendEffect { AuthEffect.SignUpSuccess }
                        },
                        onFailure = { error ->
                            setState { copy(isLoading = false) }
                            sendNetworkError(error.toNetworkError()) { errorMessage ->
                                sendEffect { AuthEffect.ShowError(errorMessage) }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun validateLogin(): Boolean {
        val state = uiState.value
        var valid = true

        if (state.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            setState { copy(emailError = "Please enter a valid email") }
            valid = false
        }
        if (state.password.isBlank()) {
            setState { copy(passwordError = "Password cannot be empty") }
            valid = false
        }
        return valid
    }

    private fun validateSignUp(): Boolean {
        val state = uiState.value
        var valid = true

        if (state.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            setState { copy(emailError = "Please enter a valid email") }
            valid = false
        }
        if (state.password.isBlank()) {
            setState { copy(passwordError = "Password cannot be empty") }
            valid = false
        }
        if (state.password != state.confirmPassword) {
            setState { copy(
                passwordError        = "Passwords should be matched",
                confirmPasswordError = "Passwords should be matched",
            ) }
            valid = false
        }
        return valid
    }
}