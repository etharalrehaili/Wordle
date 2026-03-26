package com.khammin.authentication.presentation.vm

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.khammin.authentication.R
import com.khammin.core.R as CoreRes
import com.khammin.authentication.domain.usecase.LoginUseCase
import com.khammin.authentication.domain.usecase.ReAuthenticateUseCase
import com.khammin.authentication.domain.usecase.SendEmailUseCase
import com.khammin.authentication.domain.usecase.SignUpUseCase
import com.khammin.authentication.presentation.contract.AuthEffect
import com.khammin.authentication.presentation.contract.AuthIntent
import com.khammin.authentication.presentation.contract.AuthUiState
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.mvi.toNetworkError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val sendEmailUseCase: SendEmailUseCase,
    private val reAuthenticateUseCase: ReAuthenticateUseCase
    ) : BaseMviViewModel<AuthIntent, AuthUiState, AuthEffect>(
    initialState = AuthUiState()
) {

    init {
        // Auto-fill email if user is logged in
        val currentEmail = FirebaseAuth.getInstance()
            .currentUser?.email
        if (currentEmail != null) {
            setState { copy(email = currentEmail) }
        }
    }

    override fun onEvent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.OnEmailChanged -> {
                setState { copy(email = intent.email, emailError = null) }
            }

            is AuthIntent.OnPasswordChanged -> {
                setState {
                    copy(
                        password = intent.password,
                        passwordError = null,
                        confirmPasswordError = null
                    )
                }
            }

            is AuthIntent.OnConfirmPasswordChanged -> {
                setState {
                    copy(
                        confirmPassword = intent.password,
                        confirmPasswordError = null,
                        passwordError = null,
                    )
                }
            }

            is AuthIntent.OnReAuthPasswordChanged ->
                setState { copy(reAuthPassword = intent.password, reAuthPasswordError = null) }

            AuthIntent.OnLoginClick -> {
                if (!validateLogin()) return
                setState { copy(isLoading = true) }
                viewModelScope.launch {
                    loginUseCase(
                        email = uiState.value.email,
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
                        email = uiState.value.email,
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

            AuthIntent.OnSendEmailClicked -> {
                val email = uiState.value.email
                if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email)
                        .matches()
                ) {
                    setState { copy(emailError = UiText.StringRes(CoreRes.string.error_invalid_email_format)) }
                    return
                }
                setState { copy(isLoading = true) }
                viewModelScope.launch {
                    sendEmailUseCase(email).fold(
                        onSuccess = {
                            setState { copy(isLoading = false) }
                            sendEffect { AuthEffect.ResetPasswordEmailSent }
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

        if (state.email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            setState { copy(emailError = UiText.StringRes(CoreRes.string.error_invalid_email_format)) }
            valid = false
        }
        if (state.password.isBlank()) {
            setState { copy(passwordError = UiText.StringRes(R.string.error_empty_password)) }
            valid = false
        }
        return valid
    }

    private fun validateSignUp(): Boolean {
        val state = uiState.value
        var valid = true

        if (state.email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            setState { copy(emailError = UiText.StringRes(CoreRes.string.error_invalid_email_format)) }
            valid = false
        }
        if (state.password.isBlank()) {
            setState { copy(passwordError = UiText.StringRes(R.string.error_empty_password)) }
            valid = false
        }
        if (state.password != state.confirmPassword) {
            setState {
                copy(
                    passwordError         = UiText.StringRes(R.string.error_passwords_not_match),
                    confirmPasswordError  = UiText.StringRes(R.string.error_passwords_not_match),
                )
            }
            valid = false
        }
        return valid
    }
}