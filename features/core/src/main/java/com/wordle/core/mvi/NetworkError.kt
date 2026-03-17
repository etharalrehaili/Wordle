package com.wordle.core.mvi

import UiText
import com.wordle.core.R

sealed interface NetworkFailure {
    /** No internet connection available */
    data object Connection : NetworkFailure

    /** Device is online but cannot reach the server */
    data object Connectivity : NetworkFailure

    /** Request timed out */
    data object Timeout : NetworkFailure

    /** 4xx errors (except 401) */
    data class Client(val code: Int) : NetworkFailure

    /** 5xx errors */
    data class Server(val code: Int) : NetworkFailure

    /** 401 Unauthorized */
    data object Unauthorized : NetworkFailure

    /** Anything else */
    data class Unexpected(val cause: Throwable? = null) : NetworkFailure
}

// ── Network Validation ────────────────────────────────────────────────────────

data class NetworkValidation(
    val field: String,
    val message: String,
)

// ── Network Error ─────────────────────────────────────────────────────────────

data class NetworkError(
    val networkFailure: NetworkFailure?       = null,
    val networkValidation: NetworkValidation? = null,
    val remoteMessage: UiText? = null,
) {
    val hasValidationError: Boolean
        get() = networkValidation != null
}

// ── Throwable → NetworkError mapper ──────────────────────────────────────────

fun Throwable.toNetworkError(): NetworkError {
    val message = message?.lowercase() ?: ""
    return when {
        message.contains("credential is incorrect", ignoreCase = true) ||
                message.contains("invalid login credentials", ignoreCase = true) ||
                message.contains("password is invalid", ignoreCase = true) ||
                message.contains("malformed", ignoreCase = true) ->
            NetworkError(remoteMessage = UiText.StringRes(R.string.error_invalid_credentials))

        message.contains("no user record", ignoreCase = true) ||
                message.contains("user not found", ignoreCase = true) ->
            NetworkError(remoteMessage = UiText.StringRes(R.string.error_user_not_found))

        message.contains("email address is already in use", ignoreCase = true) ->
            NetworkError(remoteMessage = UiText.StringRes(R.string.error_email_already_exists))

        message.contains("too many requests", ignoreCase = true) ->
            NetworkError(remoteMessage = UiText.StringRes(R.string.error_too_many_requests))

        message.contains("email address is badly formatted", ignoreCase = true) ->
            NetworkError(remoteMessage = UiText.StringRes(R.string.error_invalid_email_format))

        this is java.net.UnknownHostException ||
                message.contains("unable to resolve host") ->
            NetworkError(
                networkFailure = NetworkFailure.Connection,
                remoteMessage  = UiText.StringRes(R.string.error_no_internet)
            )

        this is java.net.ConnectException ||
                message.contains("connection refused") ||
                message.contains("recaptcha") ->
            NetworkError(
                networkFailure = NetworkFailure.Connectivity,
                remoteMessage  = UiText.StringRes(R.string.error_connection)
            )

        this is java.net.SocketTimeoutException ||
                message.contains("timeout") ->
            NetworkError(
                networkFailure = NetworkFailure.Timeout,
                remoteMessage  = UiText.StringRes(R.string.error_timeout)
            )

        else ->
            NetworkError(
                networkFailure = NetworkFailure.Unexpected(this),
                remoteMessage  = UiText.StringRes(R.string.error_unexpected),
            )
    }
}