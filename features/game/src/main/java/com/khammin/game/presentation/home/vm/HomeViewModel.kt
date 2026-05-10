package com.khammin.game.presentation.home.vm

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import java.util.Locale
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.khammin.authentication.domain.usecase.GetAuthStateUseCase
import com.khammin.core.domain.model.GameRoom
import com.khammin.core.domain.model.RoomStatus
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.util.NetworkUtils
import com.khammin.core.util.Resource
import kotlinx.coroutines.tasks.await
import com.khammin.game.domain.usecases.challenge.GetChallengeSolvedStateUseCase
import com.khammin.game.domain.usecases.challenges.InitializeChallengeProgressUseCase
import com.khammin.game.domain.usecases.game.CreateRoomUseCase
import com.khammin.game.domain.usecases.game.FindRoomByCodeUseCase
import com.khammin.game.domain.usecases.game.GetRoomUseCase
import com.khammin.game.domain.usecases.game.GetGameProgressUseCase
import com.khammin.game.domain.usecases.game.GetWordsUseCase
import com.khammin.game.domain.usecases.game.JoinRoomUseCase
import com.khammin.game.domain.usecases.profile.CreateProfileUseCase
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import com.khammin.game.domain.usecases.profile.ObserveProfileUseCase
import com.khammin.game.domain.usecases.stats.MigrateLocalStatsUseCase
import com.khammin.game.presentation.home.contract.HomeEffect
import com.khammin.game.presentation.home.contract.HomeIntent
import com.khammin.game.presentation.home.contract.HomeUiState
import com.khammin.game.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home screen.
 *
 * Manages authentication state, word-length unlock progress, multiplayer room
 * creation/joining, and the visibility of all bottom sheets on the home screen.
 *
 * Follows the MVI pattern via [BaseMviViewModel]: the UI dispatches [HomeIntent]s,
 * and this ViewModel produces a new [HomeUiState] for each change.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    getAuthState : GetAuthStateUseCase,
    getChallengeSolvedState: GetChallengeSolvedStateUseCase,
    getGameProgressUseCase: GetGameProgressUseCase,
    private val getProfileUseCase         : GetProfileUseCase,
    private val observeProfileUseCase     : ObserveProfileUseCase,
    private val createProfileUseCase      : CreateProfileUseCase,
    private val migrateLocalStatsUseCase  : MigrateLocalStatsUseCase,
    private val createRoomUseCase: CreateRoomUseCase,
    private val joinRoomUseCase: JoinRoomUseCase,
    private val addGuestToRoomUseCase: com.khammin.game.domain.usecases.game.AddGuestToRoomUseCase,
    private val findRoomByCodeUseCase: FindRoomByCodeUseCase,
    private val getRoomUseCase       : GetRoomUseCase,
    private val getWordsUseCase: GetWordsUseCase,
    private val networkUtils: NetworkUtils,
    private val initializeChallengeProgressUseCase: InitializeChallengeProgressUseCase,
) : BaseMviViewModel<HomeIntent, HomeUiState, HomeEffect>(
    initialState = HomeUiState()
) {

    // SharedPreferences used only to persist the "welcome sheet shown" flag.
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    init {
        // 1. Ensure every user (including first-time visitors) has a Firebase
        //    identity before any network operation is attempted.
        viewModelScope.launch {
            ensureAnonymousAuth()
        }

        // 2. React to auth state changes (sign-in / sign-out).
        //    When a real (non-anonymous) user is detected, trigger profile setup.
        viewModelScope.launch {
            getAuthState().collect {
                val user = auth.currentUser
                val isRealUser = user != null && !user.isAnonymous
                val verified = isRealUser && user.isEmailVerified == true
                setState { copy(isLoggedIn = isRealUser, isEmailVerified = verified) }
                if (isRealUser) ensureProfileExists()
            }
        }

        // 3. Observe local game progress (DataStore) for unlock calculations.
        //    For guests we use local progress directly; for logged-in users the
        //    Firestore profile is the source of truth (handled in collector 4).
        viewModelScope.launch {
            getGameProgressUseCase().collect { progress ->
                setState {
                    val user = auth.currentUser
                    val isGuest = user == null || user.isAnonymous
                    copy(
                        easyWordsSolved    = if (isGuest) progress.easyWordsSolved else easyWordsSolved,
                        classicWordsSolved = progress.classicWordsSolved,
                    )
                }
            }
        }

        // 4. Observe the Firestore profile for logged-in users so that
        //    easyWordsSolved stays in sync with the server (e.g. across devices).
        //    flatMapLatest restarts the profile observation whenever auth changes.
        viewModelScope.launch {
            getAuthState()
                .flatMapLatest { _ ->
                    val user = auth.currentUser
                    if (user != null && !user.isAnonymous) {
                        observeProfileUseCase(user.uid)
                    } else {
                        flowOf(null)
                    }
                }
                .collect { profile ->
                    if (profile != null) {
                        setState {
                            copy(easyWordsSolved = profile.arWordsSolved)
                        }
                    }
                }
        }

        // 5. Show the countdown row if the user has solved today's challenge in
        //    either language (EN or AR).
        viewModelScope.launch {
            getChallengeSolvedState("en").combine(
                getChallengeSolvedState("ar")
            ) { enSolved, arSolved ->
                enSolved || arSolved
            }.collect { solved ->
                setState { copy(hasSolvedChallenge = solved) }
            }
        }
    }

    /**
     * Guarantees that every app launch has a Firebase identity.
     *
     * - If no user exists, signs in anonymously and assigns a guest display name.
     * - Checks whether the welcome sheet has been shown before and shows it once
     *   to brand-new installs.
     */
    private suspend fun ensureAnonymousAuth() {
        val auth = auth
        if (auth.currentUser == null) {
            try {
                auth.signInAnonymously().await()
                auth.currentUser?.let { setGuestDisplayName(it) }
            } catch (e: Exception) {
                return
            }
        }
        val hasShown = prefs.getBoolean("hasShownWelcomeSheet", false)
        if (!hasShown) {
            setState { copy(showWelcomeSheet = true) }
        }
    }

    /**
     * Sets a human-readable guest display name (e.g. "GUEST-A3F2B") on the
     * Firebase user if they don't already have one.
     * The suffix is derived from the first 5 characters of the UID so it is
     * stable across sessions but unique per user.
     */
    private suspend fun setGuestDisplayName(user: FirebaseUser) {
        if (!user.displayName.isNullOrEmpty()) return
        val guestName = "GUEST-${user.uid.take(5).uppercase()}"
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(guestName)
            .build()
        try { user.updateProfile(request).await() } catch (_: Exception) {}
    }

    /**
     * In-memory cache keyed by "$language-$length" (e.g. "ar-4").
     * Populated by [prefetchWords] so the game screen can start immediately.
     */
    private val cachedWords = mutableMapOf<String, List<String>>()

    /**
     * Pre-fetches word lists for all supported word lengths (4, 5, 6) in the
     * given language and stores them in [cachedWords].
     *
     * Called from [HomeScreen] so that by the time the user taps "Quick Play"
     * the words are already in the local Room DB and load instantly.
     * Safe to call multiple times — returns early if already cached.
     */
    fun prefetchWords(language: String) {
        if (cachedWords[language] != null) return
        viewModelScope.launch {
            listOf(4, 5, 6).forEach { length ->
                val result = getWordsUseCase(language, length)
                if (result is Resource.Success) {
                    cachedWords["$language-$length"] = result.data.map { it.text }
                }
            }
        }
    }

    /**
     * Creates a Firestore profile document for a newly signed-in user if one
     * doesn't already exist, then initializes their challenge progress and
     * migrates any locally stored stats to the server.
     *
     * Only runs for non-anonymous users; guests have no server-side profile.
     */
    private fun ensureProfileExists() {
        viewModelScope.launch {
            val user  = auth.currentUser ?: return@launch
            if (user.isAnonymous) return@launch

            val uid   = user.uid
            val email = user.email ?: uid

            when (val result = getProfileUseCase(uid)) {
                is Resource.Success -> {
                    if (result.data == null) {
                        // First sign-in: create the profile document.
                        createProfileUseCase(uid, email.substringBefore("@"))
                    }
                    // Always try to initialize challenge progress and migrate stats;
                    // these are no-ops if already done.
                    runCatching { initializeChallengeProgressUseCase(uid) }
                    runCatching { migrateLocalStatsUseCase() }
                }
                else -> Unit
            }
        }
    }

    /**
     * Creates a new multiplayer room in Firestore and returns its ID and the
     * host's user ID via [onRoomCreated].
     *
     * Room type is determined by [customWord]:
     * - `null`            → lobby mode (host picks a random word each round)
     * - empty string `""` → custom-word room (host will type a word before starting)
     * - non-empty string  → custom-word room with the word pre-set (not currently used)
     *
     * Shows [HomeUiState.noInternetError] and aborts if the device is offline.
     */
    fun createRoom(
        language: String,
        customWord: String? = null,
        onRoomCreated: (String, String) -> Unit,
    ) {
        viewModelScope.launch {
            if (!networkUtils.isConnected()) {
                setState { copy(noInternetError = true) }
                return@launch
            }

            setState { copy(createRoomLoading = true) }

            // Ensure we have a Firebase identity before writing to Firestore.
            val auth = auth
            if (auth.currentUser == null) {
                try { auth.signInAnonymously().await() } catch (_: Exception) { /* proceed as guest */ }
            }

            val myId = auth.currentUser?.uid
                ?: "guest_${System.currentTimeMillis()}"

            val isCustomWordRoom = customWord != null
            val isWordProvided   = !customWord.isNullOrEmpty()

            // Only set word/wordLength when the host has provided the actual word.
            // For lobby/custom-word rooms the host sets the word later in-game.
            val (word, wordLength) = when {
                isWordProvided   -> customWord!!.uppercase() to customWord.length
                isCustomWordRoom -> "" to 0
                else             -> "" to 0
            }

            val room = GameRoom(
                hostId       = myId,
                word         = word,
                language     = language,
                wordLength   = wordLength,
                isCustomWord = isCustomWordRoom,
                isLobbyMode  = !isCustomWordRoom,
            )

            val roomId = createRoomUseCase(room)

            setState { copy(createRoomLoading = false) }
            onRoomCreated(roomId, myId)
        }
    }

    /**
     * Builds a [Context] localized to [language] so that error strings are shown
     * in the correct language regardless of the device's system locale.
     */
    private fun localizedContext(language: String): Context {
        val locale = Locale(language)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    /**
     * Joins an existing multiplayer room identified by a short [code].
     *
     * Validation order (mirrors the Firestore security rules):
     * 1. Device must be online.
     * 2. The short code must resolve to a full Firestore document ID.
     * 3. Room must exist, not be finished, and not be already in progress.
     * 4. Room must not be full (capacity varies by room type):
     *    - 1v1 room: max 2 players (host + 1 guest)
     *    - Custom-word room: max 4 players (host + 3 guests)
     *    - Lobby-mode room: max 3 players (host + 2 guests)
     *
     * On success, adds the guest to the room and calls [onJoined] with the full
     * room ID, the guest's user ID, and flags describing the room type.
     */
    fun joinRoom(code: String, language: String = "ar", onJoined: (String, String, Boolean, Boolean) -> Unit) {
        viewModelScope.launch {
            Log.d("JoinRoomDebug", "joinRoom() called — code='${code.trim()}'")

            if (!networkUtils.isConnected()) {
                Log.d("JoinRoomDebug", "joinRoom() rejected — no internet connection")
                setState { copy(noInternetError = true) }
                return@launch
            }

            setState { copy(joinRoomLoading = true, joinRoomError = null) }

            val auth = auth
            if (auth.currentUser == null) {
                try { auth.signInAnonymously().await() } catch (_: Exception) { /* proceed as guest */ }
            }

            val myId = auth.currentUser?.uid
                ?: "guest_${System.currentTimeMillis()}"

            Log.d("JoinRoomDebug", "joinRoom() searching Firestore for shortCode='${code.trim()}' as userId='$myId'")

            // Room codes are the first 6 characters of the Firestore document ID.
            val fullRoomId = findRoomByCodeUseCase(code.trim())
            if (fullRoomId == null) {
                Log.d("JoinRoomDebug", "joinRoom() rejected — no room found matching code='${code.trim()}' (checked waiting + playing statuses)")
                setState { copy(joinRoomLoading = false, joinRoomError = localizedContext(language).getString(R.string.join_error_not_found_hint)) }
                return@launch
            }

            Log.d("JoinRoomDebug", "joinRoom() found fullRoomId='$fullRoomId' — fetching room details")

            val room = getRoomUseCase(fullRoomId)
            Log.d("JoinRoomDebug", "joinRoom() room fetched — status='${room?.status}', isLobbyMode=${room?.isLobbyMode}, isCustomWord=${room?.isCustomWord}, guestIds=${room?.guestIds}, guestId='${room?.guestId}'")

            when {
                room == null -> {
                    Log.d("JoinRoomDebug", "joinRoom() rejected — room document is null after fetch")
                    setState { copy(joinRoomLoading = false, joinRoomError = localizedContext(language).getString(R.string.join_error_not_found)) }
                    return@launch
                }
                room.status == RoomStatus.FINISHED.value -> {
                    Log.d("JoinRoomDebug", "joinRoom() rejected — room status is FINISHED")
                    setState { copy(joinRoomLoading = false, joinRoomError = localizedContext(language).getString(R.string.join_error_game_ended)) }
                    return@launch
                }
                // Custom-word room: host + up to 3 guests = 4 total
                room.isCustomWord && room.guestIds.size >= 3 -> {
                    Log.d("JoinRoomDebug", "joinRoom() rejected — custom-word room is full (${room.guestIds.size}/3 guests)")
                    setState { copy(joinRoomLoading = false, joinRoomError = localizedContext(language).getString(R.string.join_error_room_full_max4)) }
                    return@launch
                }
                // Lobby-mode room: host + up to 2 guests = 3 total
                room.isLobbyMode && room.guestIds.size >= 2 -> {
                    Log.d("JoinRoomDebug", "joinRoom() rejected — lobby room is full (${room.guestIds.size}/2 guests)")
                    setState { copy(joinRoomLoading = false, joinRoomError = localizedContext(language).getString(R.string.join_error_room_full_max3)) }
                    return@launch
                }
                // Standard 1v1 room: already has a guest
                !room.isCustomWord && !room.isLobbyMode && room.guestId.isNotEmpty() -> {
                    Log.d("JoinRoomDebug", "joinRoom() rejected — 1v1 room already has a guest ('${room.guestId}')")
                    setState { copy(joinRoomLoading = false, joinRoomError = localizedContext(language).getString(R.string.join_error_room_full)) }
                    return@launch
                }
            }

            // Multi-guest rooms use an array-union approach; 1v1 rooms set guestId directly.
            Log.d("JoinRoomDebug", "joinRoom() adding player — isLobbyMode=${room.isLobbyMode}, isCustomWord=${room.isCustomWord}, roomStatus='${room.status}'")
            if (room.isCustomWord || room.isLobbyMode) {
                addGuestToRoomUseCase(fullRoomId, myId)
            } else {
                joinRoomUseCase(fullRoomId, myId)
            }
            Log.d("JoinRoomDebug", "joinRoom() SUCCESS — userId='$myId' joined fullRoomId='$fullRoomId'")
            setState { copy(joinRoomLoading = false, joinRoomError = null) }
            onJoined(fullRoomId, myId, room.isCustomWord, room.isLobbyMode)
        }
    }

    /** Clears the join-room error message (e.g. when the user edits the code). */
    fun clearJoinRoomError() {
        setState { copy(joinRoomError = null) }
    }

    /** Clears the no-internet error flag after the sheet is dismissed. */
    fun clearNoInternetError() {
        setState { copy(noInternetError = false) }
    }

    /**
     * Persists that the welcome sheet has been shown and hides it from the UI.
     * Called both when the user picks "Sign in with Google" and "Continue as Guest"
     * so the sheet never appears again after the first launch.
     */
    fun markWelcomeSheetShown() {
        prefs.edit().putBoolean("hasShownWelcomeSheet", true).apply()
        setState { copy(showWelcomeSheet = false) }
    }

    /**
     * Signs in (or links) a Firebase account using the Google ID token received
     * from the Google Sign-In flow.
     *
     * Linking strategy:
     * - If the current user is anonymous, we attempt to *link* the Google credential
     *   to the existing anonymous account so their local progress is preserved.
     * - If linking fails because the Google account already has a Firebase account
     *   ([FirebaseAuthUserCollisionException]), we fall back to a plain sign-in,
     *   which switches to the existing account (local progress is not migrated).
     * - If there is no current user, we sign in directly.
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val auth = auth
            val currentUser = auth.currentUser
            try {
                if (currentUser != null && currentUser.isAnonymous) {
                    try {
                        // Preferred path: promote anonymous account to a real account.
                        currentUser.linkWithCredential(credential).await()
                    } catch (e: FirebaseAuthUserCollisionException) {
                        // The Google account already exists — sign in to the existing account.
                        auth.signInWithCredential(credential).await()
                    }
                } else {
                    auth.signInWithCredential(credential).await()
                }
            } catch (e: Exception) {
                // sign-in failed — user stays as-is (anonymous or previously signed in)
            }
        }
    }

    /** Handles bottom-sheet visibility and form-field intents from the UI. */
    override fun onEvent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ShowGameModeSheet    -> setState { copy(showGameModeSheet = intent.show) }
            is HomeIntent.ShowLengthSheet      -> setState { copy(showLengthSheet = intent.show) }
            is HomeIntent.ShowMultiplayerSheet -> setState { copy(showMultiplayerSheet = intent.show) }
            is HomeIntent.ShowWordPickerSheet  -> setState {
                // Clear createRoomType when the sheet is hidden so the next open starts fresh.
                copy(
                    showWordPickerSheet = intent.show,
                    createRoomType = if (!intent.show) null else createRoomType,
                )
            }
            is HomeIntent.ShowJoinRoomSheet    -> setState {
                // Clear the typed room code when the sheet is hidden.
                copy(
                    showJoinRoomSheet = intent.show,
                    joinRoomCode = if (!intent.show) "" else joinRoomCode,
                )
            }
            is HomeIntent.SetCreateRoomType    -> setState { copy(createRoomType = intent.type) }
            is HomeIntent.SetJoinRoomCode      -> setState { copy(joinRoomCode = intent.code) }
        }
    }
}