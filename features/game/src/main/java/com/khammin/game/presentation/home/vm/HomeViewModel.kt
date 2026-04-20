package com.khammin.game.presentation.home.vm

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.khammin.authentication.domain.usecase.GetAuthStateUseCase
import com.khammin.core.domain.model.GameRoom
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.util.NetworkUtils
import com.khammin.core.util.Resource
import kotlinx.coroutines.tasks.await
import com.khammin.game.domain.usecases.challenge.GetChallengeSolvedStateUseCase
import com.khammin.game.domain.usecases.game.CreateRoomUseCase
import com.khammin.game.domain.usecases.game.FindRoomByCodeUseCase
import com.khammin.game.domain.usecases.game.GetRoomUseCase
import com.khammin.game.domain.usecases.game.GetGameProgressUseCase
import com.khammin.game.domain.usecases.game.GetWordsUseCase
import com.khammin.game.domain.usecases.game.JoinRoomUseCase
import com.khammin.game.domain.usecases.profile.CreateProfileUseCase
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import com.khammin.game.presentation.home.contract.HomeEffect
import com.khammin.game.presentation.home.contract.HomeIntent
import com.khammin.game.presentation.home.contract.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAuthState : GetAuthStateUseCase,
    getChallengeSolvedState: GetChallengeSolvedStateUseCase,
    getGameProgressUseCase: GetGameProgressUseCase,
    private val getProfileUseCase   : GetProfileUseCase,
    private val createProfileUseCase: CreateProfileUseCase,
    private val createRoomUseCase: CreateRoomUseCase,
    private val joinRoomUseCase: JoinRoomUseCase,
    private val addGuestToRoomUseCase: com.khammin.game.domain.usecases.game.AddGuestToRoomUseCase,
    private val findRoomByCodeUseCase: FindRoomByCodeUseCase,
    private val getRoomUseCase       : GetRoomUseCase,
    private val getWordsUseCase: GetWordsUseCase,
    private val networkUtils: NetworkUtils,
) : BaseMviViewModel<HomeIntent, HomeUiState, HomeEffect>(
    initialState = HomeUiState()
) {

    init {
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
                    copy(
                        easyWordsSolved    = progress.easyWordsSolved,
                        classicWordsSolved = progress.classicWordsSolved,
                    )
                }
            }
        }
        viewModelScope.launch {
            // check both languages, show countdown if either is solved
            getChallengeSolvedState("en").combine(
                getChallengeSolvedState("ar")
            ) { enSolved, arSolved ->
                enSolved || arSolved
            }.collect { solved ->
                setState { copy(hasSolvedChallenge = solved) }
            }
        }
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
                }
                is Resource.Error -> {
                    createProfileUseCase(uid, email.substringBefore("@"))
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

            // Anonymous auth is required to associate the room with a host ID,
            // but we don't want to force users to create an account just to play with friends.
            // If they're not logged in, sign them in anonymously. This way they can still create/join
            // rooms and have a consistent identity across sessions until they choose to log out or create an account.
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                try { auth.signInAnonymously().await() } catch (_: Exception) { /* proceed as guest */ }
            }

            val myId = auth.currentUser?.uid
                ?: "guest_${System.currentTimeMillis()}"

            val isCustomWordRoom = customWord != null
            val isWordProvided   = !customWord.isNullOrEmpty()

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

            val room = GameRoom(
                hostId       = myId,
                word         = word,
                language     = language,
                wordLength   = wordLength,
                isCustomWord = isCustomWordRoom,
                isLobbyMode  = !isCustomWordRoom,   // true for random word path
            )

            val roomId = createRoomUseCase(room)
            setState { copy(createRoomLoading = false) }
            onRoomCreated(roomId, myId)
        }
    }

    fun joinRoom(code: String, onJoined: (String, String, Boolean, Boolean) -> Unit) {
        viewModelScope.launch {
            if (!networkUtils.isConnected()) {
                setState { copy(noInternetError = true) }
                return@launch
            }

            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                try { auth.signInAnonymously().await() } catch (_: Exception) { /* proceed as guest */ }
            }

            val myId = auth.currentUser?.uid
                ?: "guest_${System.currentTimeMillis()}"

            val fullRoomId = findRoomByCodeUseCase(code.trim())
            if (fullRoomId == null) {
                setState { copy(joinRoomLoading = false, joinRoomError = "Room not found. Check the code and try again.") }
                return@launch
            }

            // ← Check if room is still joinable
            val room = getRoomUseCase(fullRoomId)
            when {
                room == null -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = "Room not found.") }
                    return@launch
                }
                room.status == "finished" -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = "This game has already ended.") }
                    return@launch
                }
                room.status == "playing" -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = "This game has already started.") }
                    return@launch
                }
                room.isCustomWord && room.guestIds.size >= 3 -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = "This room is full (maximum 4 players).") }
                    return@launch
                }
                room.isLobbyMode && room.guestIds.size >= 2 -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = "This room is full (maximum 3 players).") }
                    return@launch
                }
                !room.isCustomWord && !room.isLobbyMode && room.guestId.isNotEmpty() -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = "This room is already full.") }
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

    override fun onEvent(intent: HomeIntent) = Unit
}