package com.wordle.core.mvi

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
    val remoteMessage: String?                = null,
) {
    val hasValidationError: Boolean
        get() = networkValidation != null
}

// ── Throwable → NetworkError mapper ──────────────────────────────────────────

fun Throwable.toNetworkError(): NetworkError {
    val message = message?.lowercase() ?: ""
    return when {
        this is java.net.UnknownHostException ||
                message.contains("unable to resolve host") ->
            NetworkError(networkFailure = NetworkFailure.Connection, remoteMessage = message)

        this is java.net.ConnectException ||
                message.contains("connection refused") ||
                message.contains("unreachable") ->
            NetworkError(networkFailure = NetworkFailure.Connectivity, remoteMessage = message)

        this is java.net.SocketTimeoutException ||
                this is kotlinx.coroutines.TimeoutCancellationException ||
                message.contains("timeout") ->
            NetworkError(networkFailure = NetworkFailure.Timeout, remoteMessage = message)

        this is java.io.IOException ||
                message.contains("network") ||
                message.contains("recaptcha") ||
                message.contains("interrupted") ->
            NetworkError(networkFailure = NetworkFailure.Connection, remoteMessage = message)

        else ->
            NetworkError(
                networkFailure = NetworkFailure.Unexpected(this),
                remoteMessage  = message,
            )
    }
}