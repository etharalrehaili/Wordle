package com.wordle.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        onError: (String) -> Unit
    ) {
        if (!handelValidationError && networkError.hasValidationError) return
        viewModelScope.launch {
            val errorMessage = when {
                networkError.hasValidationError && networkError.networkValidation != null -> {
                    // Handle validation errors
                    networkError.remoteMessage ?: "Validation error occurred"
                }
                networkError.networkFailure != null -> {
                    when (networkError.networkFailure) {
                        is NetworkFailure.Connection,
                        is NetworkFailure.Connectivity -> "Connection error. Please check your internet."
                        is NetworkFailure.Timeout -> "Request timeout. Please try again."
                        is NetworkFailure.Client -> networkError.remoteMessage ?: "Client error occurred"
                        is NetworkFailure.Server -> "Server error. Please try again later."
                        is NetworkFailure.Unauthorized -> "Unauthorized. Please login again."
                        is NetworkFailure.Unexpected -> networkError.remoteMessage ?: "Unexpected error occurred"
                    }
                }
                else -> networkError.remoteMessage ?: "An error occurred"
            }
            onError(errorMessage)
        }
    }
}

// MVI Interfaces
interface UiIntent
interface UiState
interface UiEffect

