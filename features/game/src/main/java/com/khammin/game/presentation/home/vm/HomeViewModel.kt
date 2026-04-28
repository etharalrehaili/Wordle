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

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    init {
        // 1. Ensure there is always a Firebase user (anonymous on first launch).
        //    Welcome sheet is shown only after this succeeds, on first ever launch.
        viewModelScope.launch {
            ensureAnonymousAuth()
        }

        // 2. Observe auth state changes (handles both anonymous and Google sign-in).
        viewModelScope.launch {
            getAuthState().collect {
                val user = FirebaseAuth.getInstance().currentUser
                val isRealUser = user != null && !user.isAnonymous
                val verified = isRealUser && user.isEmailVerified == true
                setState { copy(isLoggedIn = isRealUser, isEmailVerified = verified) }
                if (isRealUser) ensureProfileExists()
            }
        }

        // For guest users, both counters come from the local DataStore.
        // For logged-in users, easyWordsSolved is driven by the profile observer below
        // (which reads from the Room cache that stays in sync with Firestore stats).
        // classicWordsSolved always uses local DataStore — the profile model does not
        // track words-solved broken down by word length.
        viewModelScope.launch {
            getGameProgressUseCase().collect { progress ->
                setState {
                    val user = FirebaseAuth.getInstance().currentUser
                    val isGuest = user == null || user.isAnonymous
                    copy(
                        easyWordsSolved    = if (isGuest) progress.easyWordsSolved else easyWordsSolved,
                        classicWordsSolved = progress.classicWordsSolved,
                    )
                }
            }
        }

        // For logged-in users, override easyWordsSolved with the Firestore-backed profile
        // value (enWordsSolved + arWordsSolved). flatMapLatest re-subscribes automatically
        // whenever the auth state changes (sign-in / sign-out).
        viewModelScope.launch {
            getAuthState()
                .flatMapLatest { _ ->
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null && !user.isAnonymous) {
                        observeProfileUseCase(user.uid)
                    } else {
                        flowOf(null)
                    }
                }
                .collect { profile ->
                    if (profile != null) {
                        setState {
                            copy(easyWordsSolved = profile.enWordsSolved + profile.arWordsSolved)
                        }
                    }
                }
        }

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

    private suspend fun ensureAnonymousAuth() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            try {
                auth.signInAnonymously().await()
                auth.currentUser?.let { setGuestDisplayName(it) }
                Log.d("HomeViewModel", "Anonymous sign-in complete uid=${auth.currentUser?.uid}")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Anonymous sign-in failed: ${e.message}")
                return
            }
        }
        // Show welcome sheet once, after auth is ready.
        val hasShown = prefs.getBoolean("hasShownWelcomeSheet", false)
        if (!hasShown) {
            setState { copy(showWelcomeSheet = true) }
        }
    }

    private suspend fun setGuestDisplayName(user: FirebaseUser) {
        if (!user.displayName.isNullOrEmpty()) return
        val guestName = "GUEST-${user.uid.take(5).uppercase()}"
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(guestName)
            .build()
        try { user.updateProfile(request).await() } catch (_: Exception) {}
    }

    private val cachedWords = mutableMapOf<String, List<String>>()

    fun prefetchWords(language: String) {
        if (cachedWords[language] != null) return
        viewModelScope.launch {
            listOf(4, 5, 6).forEach { length ->
                val result = getWordsUseCase(language, length)
                if (result is Resource.Success) {
                    cachedWords["$language-$length"] = result.data
                }
            }
        }
    }

    private fun ensureProfileExists() {
        viewModelScope.launch {
            val user  = FirebaseAuth.getInstance().currentUser ?: return@launch

            if (user.isAnonymous) return@launch

            val uid   = user.uid
            val email = user.email ?: uid

            when (val result = getProfileUseCase(uid)) {
                is Resource.Success -> {
                    // Only create a profile when the server definitively says none exists.
                    // Resource.Error means a network/server failure — the profile may well
                    // exist on the server, so creating here would produce a duplicate.
                    if (result.data == null) {
                        createProfileUseCase(uid, email.substringBefore("@"))
                    }
                    // Initialize challenge progress document if it doesn't exist yet.
                    runCatching { initializeChallengeProgressUseCase(uid) }
                    // Migrate any locally-saved guest stats to the Strapi profile.
                    // This is a no-op if no local stats exist or already migrated.
                    runCatching { migrateLocalStatsUseCase() }
                }
                else -> Unit
            }
        }
    }

    fun createRoom(
        language: String,
        customWord: String? = null,
        onRoomCreated: (String, String) -> Unit,
    ) {
        viewModelScope.launch {
            val perfStart = System.currentTimeMillis()
            val roomType  = if (customWord != null) "custom" else "random"
            Log.d("RoomPerf", "[$roomType] ── START ── language=$language")

            if (!networkUtils.isConnected()) {
                setState { copy(noInternetError = true) }
                Log.d("RoomPerf", "[$roomType] Aborted: no internet | elapsed=${System.currentTimeMillis() - perfStart}ms")
                return@launch
            }
            Log.d("RoomPerf", "[$roomType] Network check passed | elapsed=${System.currentTimeMillis() - perfStart}ms")

            setState { copy(createRoomLoading = true) }

            // Anonymous auth is required to associate the room with a host ID,
            // but we don't want to force users to create an account just to play with friends.
            // If they're not logged in, sign them in anonymously. This way they can still create/join
            // rooms and have a consistent identity across sessions until they choose to log out or create an account.
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                val authStart = System.currentTimeMillis()
                Log.d("RoomPerf", "[$roomType] No authenticated user — starting anonymous sign-in | elapsed=${System.currentTimeMillis() - perfStart}ms")
                try { auth.signInAnonymously().await() } catch (_: Exception) { /* proceed as guest */ }
                Log.d("RoomPerf", "[$roomType] Anonymous sign-in done | step=${System.currentTimeMillis() - authStart}ms | elapsed=${System.currentTimeMillis() - perfStart}ms")
            } else {
                Log.d("RoomPerf", "[$roomType] User already authenticated (uid=${auth.currentUser?.uid}) | elapsed=${System.currentTimeMillis() - perfStart}ms")
            }

            val myId = auth.currentUser?.uid
                ?: "guest_${System.currentTimeMillis()}"

            val isCustomWordRoom = customWord != null
            val isWordProvided   = !customWord.isNullOrEmpty()

            val wordStart = System.currentTimeMillis()
            val (word, wordLength) = when {
                isWordProvided -> {
                    Log.d("WordleRoom", "customWord (raw)='$customWord'  stored='${customWord!!.uppercase()}'  length=${customWord.length}")
                    customWord.uppercase() to customWord.length
                }
                isCustomWordRoom -> {
                    // Host will set the word in the lobby — create room with empty word in waiting status
                    "" to 0
                }
                else -> {
                    // Random word lobby — host picks word at start time
                    "" to 0
                }
            }
            Log.d("RoomPerf", "[$roomType] Word resolved: word='$word' wordLength=$wordLength | step=${System.currentTimeMillis() - wordStart}ms | elapsed=${System.currentTimeMillis() - perfStart}ms")

            val room = GameRoom(
                hostId       = myId,
                word         = word,
                language     = language,
                wordLength   = wordLength,
                isCustomWord = isCustomWordRoom,
                isLobbyMode  = !isCustomWordRoom,   // true for random word path
            )
            Log.d("RoomPerf", "[$roomType] GameRoom object built (isCustomWord=$isCustomWordRoom isLobbyMode=${!isCustomWordRoom}) | elapsed=${System.currentTimeMillis() - perfStart}ms")

            val firestoreStart = System.currentTimeMillis()
            Log.d("RoomPerf", "[$roomType] Calling createRoomUseCase → Firestore write starting | elapsed=${System.currentTimeMillis() - perfStart}ms")
            val roomId = createRoomUseCase(room)
            Log.d("RoomPerf", "[$roomType] Firestore write ack received | step=${System.currentTimeMillis() - firestoreStart}ms | elapsed=${System.currentTimeMillis() - perfStart}ms | roomId=$roomId")

            setState { copy(createRoomLoading = false) }
            Log.d("RoomPerf", "[$roomType] ── DONE — navigating to room | total=${System.currentTimeMillis() - perfStart}ms")
            onRoomCreated(roomId, myId)
        }
    }

    private fun localizedContext(language: String): Context {
        val locale = Locale(language)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun joinRoom(code: String, language: String = "ar", onJoined: (String, String, Boolean, Boolean) -> Unit) {
        viewModelScope.launch {
            if (!networkUtils.isConnected()) {
                setState { copy(noInternetError = true) }
                return@launch
            }

            setState { copy(joinRoomLoading = true, joinRoomError = null) }

            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                try { auth.signInAnonymously().await() } catch (_: Exception) { /* proceed as guest */ }
            }

            val myId = auth.currentUser?.uid
                ?: "guest_${System.currentTimeMillis()}"

            val fullRoomId = findRoomByCodeUseCase(code.trim())
            if (fullRoomId == null) {
                setState { copy(joinRoomLoading = false, joinRoomError = localizedContext(language).getString(R.string.join_error_not_found_hint)) }
                return@launch
            }

            // ← Check if room is still joinable
            val room = getRoomUseCase(fullRoomId)
            when {
                room == null -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = localizedContext(language).getString(R.string.join_error_not_found)) }
                    return@launch
                }
                room.status == "finished" -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = localizedContext(language).getString(R.string.join_error_game_ended)) }
                    return@launch
                }
                room.status == "playing" -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = localizedContext(language).getString(R.string.join_error_game_started)) }
                    return@launch
                }
                room.isCustomWord && room.guestIds.size >= 3 -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = localizedContext(language).getString(R.string.join_error_room_full_max4)) }
                    return@launch
                }
                room.isLobbyMode && room.guestIds.size >= 2 -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = localizedContext(language).getString(R.string.join_error_room_full_max3)) }
                    return@launch
                }
                !room.isCustomWord && !room.isLobbyMode && room.guestId.isNotEmpty() -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = localizedContext(language).getString(R.string.join_error_room_full)) }
                    return@launch
                }
            }

            if (room.isCustomWord || room.isLobbyMode) {
                addGuestToRoomUseCase(fullRoomId, myId)
            } else {
                joinRoomUseCase(fullRoomId, myId)
            }
            setState { copy(joinRoomLoading = false, joinRoomError = null) }
            onJoined(fullRoomId, myId, room.isCustomWord, room.isLobbyMode)
        }
    }

    fun clearJoinRoomError() {
        setState { copy(joinRoomError = null) }
    }

    fun clearNoInternetError() {
        setState { copy(noInternetError = false) }
    }

    fun markWelcomeSheetShown() {
        prefs.edit().putBoolean("hasShownWelcomeSheet", true).apply()
        setState { copy(showWelcomeSheet = false) }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            try {
                if (currentUser != null && currentUser.isAnonymous) {
                    try {
                        currentUser.linkWithCredential(credential).await()
                    } catch (e: FirebaseAuthUserCollisionException) {
                        auth.signInWithCredential(credential).await()
                    }
                } else {
                    auth.signInWithCredential(credential).await()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Google sign-in failed: ${e.message}")
            }
        }
    }

    override fun onEvent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ShowGameModeSheet    -> setState { copy(showGameModeSheet = intent.show) }
            is HomeIntent.ShowLengthSheet      -> setState { copy(showLengthSheet = intent.show) }
            is HomeIntent.ShowMultiplayerSheet -> setState { copy(showMultiplayerSheet = intent.show) }
            is HomeIntent.ShowWordPickerSheet  -> setState {
                // Clear createRoomType when the sheet closes
                copy(
                    showWordPickerSheet = intent.show,
                    createRoomType = if (!intent.show) null else createRoomType,
                )
            }
            is HomeIntent.ShowJoinRoomSheet    -> setState {
                // Clear the typed code when the sheet closes
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