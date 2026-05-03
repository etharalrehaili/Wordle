package com.khammin.game.presentation.home.vm

import android.content.Context
import android.content.res.Configuration
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
        viewModelScope.launch {
            ensureAnonymousAuth()
        }

        viewModelScope.launch {
            getAuthState().collect {
                val user = FirebaseAuth.getInstance().currentUser
                val isRealUser = user != null && !user.isAnonymous
                val verified = isRealUser && user.isEmailVerified == true
                setState { copy(isLoggedIn = isRealUser, isEmailVerified = verified) }
                if (isRealUser) ensureProfileExists()
            }
        }

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
            } catch (e: Exception) {
                return
            }
        }
        auth.currentUser?.uid?.let { uid ->
            if (auth.currentUser?.isAnonymous == true) {
                runCatching { initializeChallengeProgressUseCase(uid) }
            }
        }
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
                    if (result.data == null) {
                        createProfileUseCase(uid, email.substringBefore("@"))
                    }
                    runCatching { initializeChallengeProgressUseCase(uid) }
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
            if (!networkUtils.isConnected()) {
                setState { copy(noInternetError = true) }
                return@launch
            }

            setState { copy(createRoomLoading = true) }

            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                try { auth.signInAnonymously().await() } catch (_: Exception) { /* proceed as guest */ }
            }

            val myId = auth.currentUser?.uid
                ?: "guest_${System.currentTimeMillis()}"

            val isCustomWordRoom = customWord != null
            val isWordProvided   = !customWord.isNullOrEmpty()

            val (word, wordLength) = when {
                isWordProvided -> customWord!!.uppercase() to customWord.length
                isCustomWordRoom -> "" to 0
                else -> "" to 0
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
                // Sign-in failed silently
            }
        }
    }

    override fun onEvent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ShowGameModeSheet    -> setState { copy(showGameModeSheet = intent.show) }
            is HomeIntent.ShowLengthSheet      -> setState { copy(showLengthSheet = intent.show) }
            is HomeIntent.ShowMultiplayerSheet -> setState { copy(showMultiplayerSheet = intent.show) }
            is HomeIntent.ShowWordPickerSheet  -> setState {
                copy(
                    showWordPickerSheet = intent.show,
                    createRoomType = if (!intent.show) null else createRoomType,
                )
            }
            is HomeIntent.ShowJoinRoomSheet    -> setState {
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