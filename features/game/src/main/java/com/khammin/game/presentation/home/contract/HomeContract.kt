package com.khammin.game.presentation.home.contract

import com.khammin.core.mvi.UiEffect
import com.khammin.core.mvi.UiIntent
import com.khammin.core.mvi.UiState

/**
 * Represents the complete UI state of the Home screen.
 *
 * This state is produced by [HomeViewModel] and consumed by [HomeScreen] / [HomeContent].
 * All bottom-sheet visibility flags live here so the ViewModel — not the UI — owns
 * navigation state, making it survive configuration changes.
 */
data class HomeUiState(

    // ── Auth & profile loading ────────────────────────────────────────────────

    /** True while the initial auth/profile check is in progress. */
    val isLoading: Boolean = false,

    /** True when the current user is signed in with a real account (not anonymous). */
    val isLoggedIn: Boolean = false,

    /**
     * True when the signed-in user has verified their email address.
     * Only meaningful when [isLoggedIn] is true.
     */
    val isEmailVerified: Boolean = false,

    // ── Challenge state ───────────────────────────────────────────────────────

    /**
     * True when the user has already solved today's challenge (in any language).
     * When true the countdown timer row is shown so the user knows when the
     * next challenge unlocks.
     */
    val hasSolvedChallenge: Boolean = false,

    // ── Word-length unlock progress ───────────────────────────────────────────

    /**
     * Number of 4-letter (easy) Arabic words the user has solved.
     * Used by [WordLengthSelectionBottomSheet] to calculate unlock progress
     * for the classic (5-letter) word length.
     */
    val easyWordsSolved: Int = 0,

    /**
     * Number of 5-letter (classic) Arabic words the user has solved.
     * Used to calculate unlock progress for the hard (6-letter) word length.
     */
    val classicWordsSolved: Int = 0,

    // ── Multiplayer room creation ─────────────────────────────────────────────

    /** True while a create-room network request is in flight. */
    val createRoomLoading: Boolean = false,

    /**
     * Tracks which room-creation type is currently loading ("random" or "custom").
     * Used to show a spinner on the correct button in [CreateRoomWordBottomSheet].
     * Null when no creation is in progress.
     */
    val createRoomType: String? = null,

    // ── Multiplayer room joining ──────────────────────────────────────────────

    /** True while a join-room network request is in flight. */
    val joinRoomLoading: Boolean = false,

    /**
     * Localized error message shown in [JoinRoomBottomSheet] when the join
     * attempt fails (room not found, already started, full, etc.).
     * Null when there is no error.
     */
    val joinRoomError: String? = null,

    /** The room-code string the user is typing in the join-room input field. */
    val joinRoomCode: String = "",

    // ── Connectivity ─────────────────────────────────────────────────────────

    /**
     * True when a multiplayer action (create / join room) was attempted without
     * an active internet connection. Triggers [NoInternetBottomSheet].
     */
    val noInternetError: Boolean = false,

    // ── Bottom-sheet visibility flags ─────────────────────────────────────────

    /**
     * Controls the one-time welcome sheet shown to brand-new users.
     * Dismissed permanently once the user picks "Sign in with Google" or
     * "Continue as Guest".
     */
    val showWelcomeSheet: Boolean = false,

    /** Controls the Game Mode sheet (Single Player vs Multiplayer). */
    val showGameModeSheet: Boolean = false,

    /** Controls the Word Length selection sheet (4 / 5 / 6 letters). */
    val showLengthSheet: Boolean = false,

    /** Controls the Multiplayer Mode sheet (Create Room vs Join Room). */
    val showMultiplayerSheet: Boolean = false,

    /** Controls the word-picker sheet where the host picks random or custom word. */
    val showWordPickerSheet: Boolean = false,

    /** Controls the join-room sheet where the guest enters a room code. */
    val showJoinRoomSheet: Boolean = false,

) : UiState

/**
 * One-time side effects emitted by [HomeViewModel].
 *
 * Currently the Home screen has no one-shot effects — all navigation is handled
 * via callbacks passed down from the NavGraph. This sealed interface is kept as
 * a placeholder for future effects (e.g. showing a toast on auth failure).
 */
sealed interface HomeEffect : UiEffect

/**
 * User actions dispatched from [HomeScreen] / [HomeContent] to [HomeViewModel].
 *
 * All intents follow the MVI pattern: the UI sends an intent, the ViewModel
 * processes it and emits a new [HomeUiState].
 */
sealed class HomeIntent : UiIntent {

    // ── Bottom-sheet visibility toggles ───────────────────────────────────────

    /** Show or hide the Game Mode selection sheet. */
    data class ShowGameModeSheet(val show: Boolean) : HomeIntent()

    /** Show or hide the Word Length selection sheet. */
    data class ShowLengthSheet(val show: Boolean) : HomeIntent()

    /** Show or hide the Multiplayer Mode sheet. */
    data class ShowMultiplayerSheet(val show: Boolean) : HomeIntent()

    /**
     * Show or hide the word-picker sheet.
     * Hiding it also clears [HomeUiState.createRoomType].
     */
    data class ShowWordPickerSheet(val show: Boolean) : HomeIntent()

    /**
     * Show or hide the join-room sheet.
     * Hiding it also clears [HomeUiState.joinRoomCode].
     */
    data class ShowJoinRoomSheet(val show: Boolean) : HomeIntent()

    // ── Form field updates ────────────────────────────────────────────────────

    /**
     * Records which room-creation mode the host selected ("random" or "custom").
     * Used to show a loading spinner on the correct button while the room is
     * being created in Firestore.
     */
    data class SetCreateRoomType(val type: String?) : HomeIntent()

    /** Updates the room-code input field as the user types. */
    data class SetJoinRoomCode(val code: String) : HomeIntent()
}
