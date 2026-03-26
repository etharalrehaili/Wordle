package com.khammin.core.mvi

import UiText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khammin.core.R
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseMviViewModel<I : UiIntent, S : UiState, E : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<E>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val uiEffect: SharedFlow<E> = _uiEffect.asSharedFlow()

    abstract fun onEvent(intent: I)

    protected fun setState(reducer: S.() -> S) {
        _uiState.update { it.reducer() }
    }

    protected fun sendEffect(builder: () -> E) {
        viewModelScope.launch {
            _uiEffect.emit(builder())
        }
    }

    protected fun sendNetworkError(
        networkError: NetworkError,
        handelValidationError: Boolean = true,
        onError: (UiText) -> Unit
    ) {
        if (!handelValidationError && networkError.hasValidationError) return
        viewModelScope.launch {
            val errorMessage: UiText = when {
                networkError.hasValidationError && networkError.networkValidation != null -> {
                    networkError.remoteMessage ?: UiText.StringRes(R.string.error_unexpected)
                }

                networkError.networkFailure != null -> {
                    when (networkError.networkFailure) {
                        is NetworkFailure.Connection,
                        is NetworkFailure.Connectivity -> UiText.StringRes(R.string.error_connection)

                        is NetworkFailure.Timeout -> UiText.StringRes(R.string.error_timeout)
                        is NetworkFailure.Client -> networkError.remoteMessage
                            ?: UiText.StringRes(R.string.error_unexpected)

                        is NetworkFailure.Server -> UiText.StringRes(R.string.error_unexpected)
                        is NetworkFailure.Unauthorized -> UiText.StringRes(R.string.error_unexpected)
                        is NetworkFailure.Unexpected -> networkError.remoteMessage
                            ?: UiText.StringRes(R.string.error_unexpected)
                    }
                }

                else -> networkError.remoteMessage ?: UiText.StringRes(R.string.error_unexpected)
            }
            onError(errorMessage)
        }
    }
}

// MVI Interfaces
interface UiIntent
interface UiState
interface UiEffect

