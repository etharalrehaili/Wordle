package com.wordle.core.mvi

import com.wordle.core.alias.Action

sealed interface UiGlobalEffect {
    data class GlobalNetworkError(
        val networkError: NetworkError? = null,
        val actions: GlobalEffectAction = GlobalEffectAction()
    ) : UiGlobalEffect

    data class NoInternetError(val actions: GlobalEffectAction = GlobalEffectAction()) : UiGlobalEffect
}

data class GlobalEffectAction(
    val showPrimaryAction: Boolean = true,
    val showSecondaryAction: Boolean = true,
//    @StringRes val primaryActionText: Int = R.string.try_again,
//    @StringRes val secondaryActionText: Int = R.string.close,
    val onPrimaryActionClick: Action? = null,
    val onSecondaryActionClick: Action? = null,
)