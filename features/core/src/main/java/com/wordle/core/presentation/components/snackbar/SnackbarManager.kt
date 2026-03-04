package com.wordle.core.presentation.components.snackbar

sealed class SnackBarEvent {
    data object Dismiss : SnackBarEvent()
}