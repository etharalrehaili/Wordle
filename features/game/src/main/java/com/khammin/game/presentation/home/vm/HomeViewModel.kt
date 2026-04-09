package com.khammin.game.presentation.home.vm

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.khammin.authentication.domain.usecase.GetAuthStateUseCase
import com.khammin.core.domain.model.GameRoom
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.util.NetworkUtils
import com.khammin.core.util.Resource
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
    private val findRoomByCodeUseCase: FindRoomByCodeUseCase,
    private val getRoomUseCase       : GetRoomUseCase,
    private val getWordsUseCase: GetWordsUseCase,
    private val networkUtils: NetworkUtils,
) : BaseMviViewModel<HomeIntent, HomeUiState, HomeEffect>(
    initialState = HomeUiState()
) {

    init {
        viewModelScope.launch {
            getAuthState().collect { isLoggedIn ->
                val verified = isLoggedIn &&
                    (FirebaseAuth.getInstance().currentUser?.isEmailVerified == true)
                setState { copy(isLoggedIn = isLoggedIn, isEmailVerified = verified) }
                if (isLoggedIn) ensureProfileExists()
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
            val uid   = user.uid
            val email = user.email ?: uid

            // Only create if no profile exists yet
            when (val result = getProfileUseCase(uid)) {
                is Resource.Success -> {
                    if (result.data == null) {
                        createProfileUseCase(uid, email.substringBefore("@"))
                    }
                }
                is Resource.Error -> {
                    // Profile fetch failed — attempt creation anyway as a fallback
                    createProfileUseCase(uid, email.substringBefore("@"))
                }
                else -> Unit
            }
        }
    }

    fun createRoom(language: String, onRoomCreated: (String, String) -> Unit) {
        viewModelScope.launch {
            if (!networkUtils.isConnected()) {
                setState { copy(noInternetError = true) }
                return@launch
            }

            val myId = FirebaseAuth.getInstance().currentUser?.uid
                ?: "guest_${System.currentTimeMillis()}"

            val wordLength = listOf(4, 5, 6).random()
            val cacheKey = "$language-$wordLength"

            // Use cache if available, otherwise fetch
            val words = cachedWords[cacheKey] ?: run {
                val result = getWordsUseCase(language, wordLength)
                if (result is Resource.Success) {
                    cachedWords[cacheKey] = result.data
                    result.data
                } else {
                    setState { copy(createRoomLoading = false) }
                    return@launch
                }
            }

            val word = words.randomOrNull() ?: run {
                setState { copy(createRoomLoading = false) }
                return@launch
            }

            val room = GameRoom(
                hostId     = myId,
                word       = word.uppercase(),
                language   = language,
                wordLength = wordLength
            )

            val roomId = createRoomUseCase(room)
            setState { copy(createRoomLoading = false) }
            onRoomCreated(roomId, myId)
        }
    }

    fun joinRoom(code: String, onJoined: (String, String) -> Unit) {
        viewModelScope.launch {
            if (!networkUtils.isConnected()) {
                setState { copy(noInternetError = true) }
                return@launch
            }

            val myId = FirebaseAuth.getInstance().currentUser?.uid
                ?: "guest_${System.currentTimeMillis()}"

            val fullRoomId = findRoomByCodeUseCase(code.trim())
            if (fullRoomId == null) {
                setState { copy(joinRoomLoading = false, joinRoomError = "Room not found. Check the code and try again.") }
                return@launch
            }

            // ← Check if room is still joinable
            val room = getRoomUseCase(fullRoomId)   // see step 3 below
            when {
                room == null -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = "Room not found.") }
                    return@launch
                }
                room.status == "finished" -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = "This game has already ended.") }
                    return@launch
                }
                room.status == "playing" || room.guestId.isNotEmpty() -> {
                    setState { copy(joinRoomLoading = false, joinRoomError = "This room is already full.") }
                    return@launch
                }
            }

            joinRoomUseCase(fullRoomId, myId)
            setState { copy(joinRoomLoading = false, joinRoomError = null) }
            onJoined(fullRoomId, myId)
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