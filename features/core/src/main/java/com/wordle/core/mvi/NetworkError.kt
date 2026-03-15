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
        // ── Firebase Auth errors ──────────────────────────────────
        message.contains("credential is incorrect", ignoreCase = true) ||
                message.contains("invalid login credentials", ignoreCase = true) ||
                message.contains("password is invalid", ignoreCase = true) ||
                message.contains("malformed", ignoreCase = true) ->
            NetworkError(remoteMessage = "Email or password is incorrect")

        message.contains("no user record", ignoreCase = true) ||
                message.contains("user not found", ignoreCase = true) ->
            NetworkError(remoteMessage = "No account found with this email")

        message.contains("email address is already in use", ignoreCase = true) ->
            NetworkError(remoteMessage = "An account with this email already exists")

        message.contains("too many requests", ignoreCase = true) ->
            NetworkError(remoteMessage = "Too many attempts. Please try again later")

        message.contains("email address is badly formatted", ignoreCase = true) ->
            NetworkError(remoteMessage = "Please enter a valid email address")

        // ── Network errors ────────────────────────────────────────
        this is java.net.UnknownHostException ||
                message.contains("unable to resolve host") ->
            NetworkError(networkFailure = NetworkFailure.Connection, remoteMessage = "No internet connection")

        this is java.net.ConnectException ||
                message.contains("connection refused") ||
                message.contains("recaptcha") ->
            NetworkError(networkFailure = NetworkFailure.Connectivity, remoteMessage = "Connection error. Please check your internet")

        this is java.net.SocketTimeoutException ||
                message.contains("timeout") ->
            NetworkError(networkFailure = NetworkFailure.Timeout, remoteMessage = "Request timed out. Please try again")

        else ->
            NetworkError(
                networkFailure = NetworkFailure.Unexpected(this),
                remoteMessage  = "Something went wrong. Please try again",
            )
    }
}