package com.khammin.game.presentation.game.vm

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.domain.model.PlayerState
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.presentation.components.GuessRow
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.enums.TileState
import com.khammin.core.presentation.components.toGuessRows
import com.khammin.core.util.Resource
import com.khammin.core.util.normalizeForWordle
import com.khammin.game.domain.usecases.game.AddGuestToRoomUseCase
import com.khammin.game.domain.usecases.game.FinishRoomUseCase
import com.khammin.game.domain.usecases.game.GetRoomUseCase
import com.khammin.game.domain.usecases.game.GetWordsUseCase
import com.khammin.game.domain.usecases.game.LeaveRoomUseCase
import com.khammin.game.domain.usecases.game.ObserveOpponentUseCase
import com.khammin.game.domain.usecases.game.ObserveRoomUseCase
import com.khammin.game.domain.usecases.game.RestartRoomUseCase
import com.khammin.game.domain.usecases.game.RemoveGuestFromRoomUseCase
import com.khammin.game.domain.usecases.game.StartRoomUseCase
import com.khammin.game.domain.usecases.game.UpdateGuestProfileUseCase
import com.khammin.game.domain.model.GameMode
import com.khammin.game.domain.model.GameResult
import com.khammin.game.domain.usecases.game.SetPlayerReadyUseCase
import com.khammin.game.domain.usecases.game.UpdatePlayerStateUseCase
import com.khammin.game.domain.usecases.game.ValidateWordUseCase
import com.khammin.game.presentation.game.contract.MultiplayerGameEffect
import com.khammin.game.presentation.game.contract.MultiplayerGameIntent
import com.khammin.game.presentation.game.contract.MultiplayerGameUiState
import com.khammin.game.presentation.game.contract.OpponentProgress
import com.khammin.game.presentation.game.contract.Tile
import com.khammin.game.presentation.game.contract.WaitingPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MultiplayerGameViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val addGuestToRoomUseCase: AddGuestToRoomUseCase,
    private val observeRoomUseCase: ObserveRoomUseCase,
    private val observeOpponentUseCase: ObserveOpponentUseCase,
    private val updatePlayerStateUseCase: UpdatePlayerStateUseCase,
    private val finishRoomUseCase: FinishRoomUseCase,
    private val leaveRoomUseCase: LeaveRoomUseCase,
    private val restartRoomUseCase: RestartRoomUseCase,
    private val getRoomUseCase: GetRoomUseCase,
    private val getWordsUseCase: GetWordsUseCase,
    private val validateWordUseCase: ValidateWordUseCase,
    private val startRoomUseCase: StartRoomUseCase,
    private val removeGuestFromRoomUseCase: RemoveGuestFromRoomUseCase,
    private val updateGuestProfileUseCase: UpdateGuestProfileUseCase,
    private val setPlayerReadyUseCase: SetPlayerReadyUseCase,
    private val auth: FirebaseAuth,
    private val presenceManager: MultiplayerPresenceManager,
    private val profileLoader: MultiplayerProfileLoader,
    private val resultHandler: MultiplayerGameResultHandler,
) : BaseMviViewModel<MultiplayerGameIntent, MultiplayerGameUiState, MultiplayerGameEffect>(
    initialState = MultiplayerGameUiState()
) {

    private var gameStartTime = 0L
    private val isAppForegroundFlow = MutableStateFlow(true)
    // Timestamp (ms) when the app last went to background; 0 = never or already foregrounded.
    private var backgroundAt: Long = 0L

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                isAppForegroundFlow.value = true
                val s = uiState.value
                // Self-disconnect kick: if we were in an active game and offline for > 60 seconds,
                // show the "You've been disconnected" sheet instead of resuming.
                val offlineDuration = if (backgroundAt > 0L) System.currentTimeMillis() - backgroundAt else 0L
                backgroundAt = 0L
                if (offlineDuration > 60_000L && s.roomId.isNotEmpty() && !s.isGameOver) {
                    sendEffect { MultiplayerGameEffect.SelfDisconnected }
                    return
                }
                if (s.roomId.isNotEmpty() && s.myUserId.isNotEmpty()) {
                    viewModelScope.launch {
                        if (s.isAnonymous) {
                            // Force-refresh the Firebase auth token before updating presence.
                            // For anonymous users the WebSocket reconnect fails auth validation
                            // repeatedly (exponential backoff ~30s) until the token is refreshed.
                            // A forced refresh here ensures RTDB authenticates on the first attempt.
                            android.util.Log.d("PresenceDebug", "[tokenRefresh] userType=guest | uid=${s.myUserId} | time=${System.currentTimeMillis()}")
                            runCatching { auth.currentUser?.getIdToken(true)?.await() }
                        }
                        presenceManager.updateForeground(s.roomId, s.myUserId, isForeground = true)
                    }
                }
                // Custom-word guest: check if we were removed from guestIds while away
                if (s.isCustomWord && !s.isHost && s.roomId.isNotEmpty() && s.myUserId.isNotEmpty()) {
                    viewModelScope.launch {
                        val room = runCatching { getRoomUseCase(s.roomId) }.getOrNull()
                        if (room != null && room.status in listOf("waiting", "playing")
                            && s.myUserId !in room.guestIds) {
                            sendEffect { MultiplayerGameEffect.ShowRejoinSheet }
                        }
                    }
                }
            }
            override fun onStop(owner: LifecycleOwner) {
                isAppForegroundFlow.value = false
                backgroundAt = System.currentTimeMillis()
                val s = uiState.value
                if (s.roomId.isNotEmpty() && s.myUserId.isNotEmpty()) {
                    viewModelScope.launch {
                        presenceManager.updateForeground(s.roomId, s.myUserId, isForeground = false)
                    }
                }
            }
        })
    }

    override fun onEvent(intent: MultiplayerGameIntent) {
        when (intent) {
            is MultiplayerGameIntent.LoadGame -> loadGame(
                intent.roomId, intent.language, intent.isHost, intent.myUserId,
                intent.isCustomWord, intent.isLobbyMode, intent.defaultMyName, intent.defaultGuestName
            )
            is MultiplayerGameIntent.EnterLetter -> enterLetter(intent.letter)
            MultiplayerGameIntent.DeleteLetter   -> deleteLetter()
            MultiplayerGameIntent.SubmitGuess    -> submitGuess()
            MultiplayerGameIntent.LeaveMatch          -> leaveMatch()
            MultiplayerGameIntent.RestartGame         -> restartGame()
            MultiplayerGameIntent.StartMatch                -> startMatch()
            is MultiplayerGameIntent.StartMatchWithWord     -> startMatchWithWord(intent.word)
            is MultiplayerGameIntent.PlayAgainCustomWord    -> playAgainCustomWord(intent.newWord)
            MultiplayerGameIntent.VotePlayAgain          -> votePlayAgain()
            MultiplayerGameIntent.PlayAgainLobbyMode     -> playAgainLobbyMode()
            MultiplayerGameIntent.RejoinRoom             -> rejoinRoom()
            is MultiplayerGameIntent.UpdateGuestProfile  -> updateGuestProfile(intent.name, intent.avatarColor, intent.avatarEmoji)
            MultiplayerGameIntent.RetryConnectivity      -> retryConnectivity()
            MultiplayerGameIntent.SetReady               -> setReady()
        }
    }

    private var observingOpponentId: String = ""
    private val observingGuestIds = mutableSetOf<String>()
    private val wordCache: MutableMap<Int, List<String>> = mutableMapOf()
    private val countdownJobs = mutableMapOf<String, Job>()

    // ── Network connectivity monitoring ───────────────────────────────────────
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var networkCallbackRegistered = false

    private fun registerNetworkCallback() {
        if (networkCallbackRegistered) return
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                setState { copy(isNoInternet = false) }
            }
            override fun onLost(network: Network) {
                setState { copy(isNoInternet = true) }
            }
        }
        cm.registerNetworkCallback(request, networkCallback!!)
        networkCallbackRegistered = true
    }

    private fun setReady() {
        val s = uiState.value
        if (s.roomId.isEmpty() || s.myUserId.isEmpty()) return
        val currentlyReady = s.waitingPlayers.firstOrNull { it.userId == s.myUserId }?.isReady == true
        val newReady = !currentlyReady
        viewModelScope.launch {
            runCatching { setPlayerReadyUseCase(s.roomId, s.myUserId, newReady) }
        }
    }

    private fun retryConnectivity() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val caps = network?.let { cm.getNetworkCapabilities(it) }
        val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        if (isConnected) setState { copy(isNoInternet = false) }
    }

    override fun onCleared() {
        super.onCleared()
        networkCallback?.let { cb ->
            runCatching {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                cm.unregisterNetworkCallback(cb)
            }
        }
        countdownJobs.values.forEach { it.cancel() }
        countdownJobs.clear()
        val s = uiState.value
        if (s.roomId.isNotEmpty() && s.myUserId.isNotEmpty()) {
            presenceManager.cleanup(s.roomId, s.myUserId)
        }
    }

    private fun loadGame(
        roomId: String,
        language: String,
        isHost: Boolean,
        myUserId: String,
        isCustomWord: Boolean,
        isLobbyMode: Boolean,
        defaultMyName: String,
        defaultGuestName: String,
    ) {
        registerNetworkCallback()

        val myId = myUserId.takeIf { it.isNotEmpty() }
            ?: auth.currentUser?.uid
            ?: uiState.value.myUserId.takeIf { it.isNotEmpty() }
            ?: return

        // Determine auth state synchronously
        val firebaseUser = auth.currentUser
        val isLoggedIn   = firebaseUser != null && !firebaseUser.isAnonymous
        val isAnonymous  = !isLoggedIn && (firebaseUser?.isAnonymous == true || myId.startsWith("guest_"))

        viewModelScope.launch {
            presenceManager.register(roomId, myId)
        }

        // For guests we can resolve the fallback name instantly; logged-in name comes from Strapi below
        val guestFallbackName = if (isAnonymous) guestNameFromId(myId) else defaultMyName

        setState {
            copy(
                roomId        = roomId, myUserId = myId, isHost = isHost,
                isCustomWord  = isCustomWord, isLobbyMode = isLobbyMode, language = language,
                defaultMyName = defaultMyName, defaultGuestName = defaultGuestName,
                myName        = guestFallbackName,
                isAnonymous   = isAnonymous,
            )
        }


        viewModelScope.launch {
            when {
                isLoggedIn -> {
                    val profile = profileLoader.loadLoggedInProfile(myId, firebaseUser?.photoUrl?.toString(), defaultMyName)
                    if (profile != null) {
                        setState { copy(myName = profile.name, avatarUrl = profile.avatarUrl) }
                        runCatching { updateGuestProfileUseCase(roomId, myId, profile.name, null, null, profile.avatarUrl) }
                    }
                }
                isAnonymous -> {
                    val profile = profileLoader.loadGuestProfile(guestFallbackName)
                    setState {
                        copy(
                            myName      = profile.name,
                            avatarColor = profile.avatarColor,
                            avatarEmoji = profile.avatarEmoji,
                            avatarUrl   = profile.avatarUrl,
                        )
                    }
                    runCatching { updateGuestProfileUseCase(roomId, myId, profile.name, profile.avatarColor, profile.avatarEmoji, profile.hostedAvatarUrl) }
                }
                else -> {
                    val profile = profileLoader.loadLoggedInProfile(myId, null, defaultMyName)
                    if (profile != null) setState { copy(myName = profile.name) }
                }
            }
        }

        listOf(4, 5, 6).forEach { length ->
            viewModelScope.launch {
                val result = getWordsUseCase(language, length)
                if (result is Resource.Success && result.data.isNotEmpty()) {
                    wordCache[length] = result.data.filter { it.length == length }
                }
            }
        }

        observeRoomUseCase(roomId).onEach { room ->
            if (room == null) return@onEach

            val isCustomWordRoom = isCustomWord || room.isCustomWord
            val isLobbyModeRoom  = isLobbyMode  || room.isLobbyMode
            val isMultiPlayer    = isCustomWordRoom || isLobbyModeRoom
            val isHostOfRoom = room.hostId == myId
            val previousWord           = uiState.value.targetWord
            val previousGuestIds       = uiState.value.guestIds
            val previousWaitingPlayers = uiState.value.waitingPlayers

            // Compute opponent ID for 1v1 (non-custom) or custom-word guest
            val opponentId = when {
                isHostOfRoom && !isMultiPlayer -> room.guestId
                isHostOfRoom                   -> ""          // host has many opponents
                else                           -> room.hostId // guest always sees host
            }

            setState {
                val boardResized = wordLength != room.wordLength
                // Rebuild waitingPlayers from room.guestIds so late-joining players immediately
                // get their saved profile (name/avatar) from guestProfiles.
                val updatedWaiting = room.guestIds.map { guestId ->
                    val existing = waitingPlayers.firstOrNull { it.userId == guestId }
                    val profile  = room.guestProfiles[guestId]
                    when {
                        profile != null -> {
                            val resolvedUrl = profile["avatarUrl"]?.takeIf { it.isNotEmpty() } ?: existing?.avatarUrl
                            (existing ?: WaitingPlayer(guestId, guestNameFromId(guestId))).copy(
                                name        = profile["name"]?.takeIf { it.isNotBlank() } ?: existing?.name ?: guestNameFromId(guestId),
                                avatarColor = profile["avatarColor"]?.toLongOrNull() ?: existing?.avatarColor,
                                avatarEmoji = profile["avatarEmoji"]?.takeIf { it.isNotEmpty() } ?: existing?.avatarEmoji,
                                avatarUrl   = resolvedUrl,
                                isReady     = profile["ready"] == "true",
                            )
                        }
                        existing != null -> existing
                        else -> WaitingPlayer(guestId, guestNameFromId(guestId)) // placeholder until fetchGuestInfo fills it in
                    }
                }
                val keepProgressIds = room.guestIds.toMutableSet()
                if (isLobbyModeRoom) keepProgressIds.add(room.hostId)
                val updatedProgress = opponentsProgress
                    .filter { it.key in keepProgressIds }
                    .mapValues { (guestId, progress) ->
                        val profile = room.guestProfiles[guestId]
                        if (profile != null) progress.copy(
                            name        = profile["name"]?.takeIf { it.isNotBlank() } ?: progress.name,
                            avatarColor = profile["avatarColor"]?.toLongOrNull() ?: progress.avatarColor,
                            avatarEmoji = profile["avatarEmoji"]?.takeIf { it.isNotEmpty() } ?: progress.avatarEmoji,
                            avatarUrl   = profile["avatarUrl"]?.takeIf { it.isNotEmpty() } ?: progress.avatarUrl,
                        ) else progress
                    }
                // For guests: apply host's saved profile if present
                val hostProfile = room.guestProfiles[room.hostId]
                val resolvedOpponentName = if (!isHostOfRoom && hostProfile != null)
                    hostProfile["name"] ?: opponentName else opponentName
                val resolvedOpponentAvatarColor = if (!isHostOfRoom && hostProfile != null)
                    hostProfile["avatarColor"]?.toLongOrNull() else opponentAvatarColor
                val resolvedOpponentAvatarEmoji = if (!isHostOfRoom && hostProfile != null)
                    hostProfile["avatarEmoji"]?.takeIf { it.isNotEmpty() } else opponentAvatarEmoji
                val resolvedOpponentAvatarUrl = if (!isHostOfRoom && hostProfile != null)
                    hostProfile["avatarUrl"]?.takeIf { it.isNotEmpty() } else opponentAvatarUrl
                copy(
                    targetWord            = room.word.uppercase(),
                    wordLength            = room.wordLength,
                    language              = if (room.word.isNotEmpty()) detectLanguage(room.word) else language,
                    opponentId            = opponentId,
                    isHost                = isHostOfRoom,
                    isCustomWord          = isCustomWordRoom,
                    isLobbyMode           = isLobbyModeRoom,
                    guestIds              = room.guestIds,
                    roomStatus            = room.status,
                    playAgainVotes        = room.playAgainVotes,
                    roundNumber           = room.roundNumber,
                    totalPoints           = room.totalPoints,
                    sessionPoints         = room.sessionPoints,
                    board                 = if (boardResized) List(board.size) { List(room.wordLength) { Tile() } } else board,
                    currentRow            = if (boardResized) 0 else currentRow,
                    currentCol            = if (boardResized) 0 else currentCol,
                    waitingPlayers        = updatedWaiting,
                    opponentsProgress     = updatedProgress,
                    opponentName          = resolvedOpponentName,
                    opponentAvatarColor   = resolvedOpponentAvatarColor,
                    opponentAvatarEmoji   = resolvedOpponentAvatarEmoji,
                    opponentAvatarUrl     = resolvedOpponentAvatarUrl,
                )
            }

            // Remove departed guests from the tracking set so they are re-observed when they rejoin.
            // In lobby mode also keep the host so we don't repeatedly start duplicate observations.
            val keepObservingIds = room.guestIds.toMutableSet()
            if (isLobbyModeRoom) keepObservingIds.add(room.hostId)
            observingGuestIds.retainAll(keepObservingIds)

            // Host: all guests left during a live game
            if (isHostOfRoom && isMultiPlayer && room.status == "playing"
                && room.guestIds.isEmpty() && previousGuestIds.isNotEmpty()) {
                sendEffect { MultiplayerGameEffect.AllPlayersLeft }
            }

            if (room.wordLength > 0 && !wordCache.containsKey(room.wordLength)) {
                viewModelScope.launch {
                    val result = getWordsUseCase(language, room.wordLength)
                    if (result is Resource.Success && result.data.isNotEmpty()) {
                        wordCache[room.wordLength] = result.data.filter { it.length == room.wordLength }
                    }
                }
            }

            // ── Host of custom-word / lobby room: observe each new guest ─────────
            if (isMultiPlayer && isHostOfRoom) {
                val newGuests = room.guestIds.filter { it !in observingGuestIds }
                for (guestId in newGuests) {
                    observingGuestIds.add(guestId)
                    observeGuestState(roomId, guestId)
                    observeOpponentPresence(roomId, guestId)
                    fetchGuestInfo(guestId)
                }
            }

            // ── Guest: detect when a co-guest leaves via guestIds shrinking ──
            if (isMultiPlayer && !isHostOfRoom && room.status in listOf("waiting", "playing")) {
                val leftIds = previousGuestIds.filter { it !in room.guestIds && it != myId }
                for (leftId in leftIds) {
                    val leftName = previousWaitingPlayers.firstOrNull { it.userId == leftId }?.name
                        ?: guestNameFromId(leftId)
                    sendEffect { MultiplayerGameEffect.GuestLeftRoom(leftName) }
                }
            }

            // ── 1v1 / multi-player guest: observe host name ──────────────────
            if (!isMultiPlayer) {
                if (opponentId.isNotEmpty() && opponentId != observingOpponentId) {
                    observingOpponentId = opponentId
                    observeOpponent(roomId, opponentId)
                    observeOpponentPresence(roomId, opponentId)
                    fetchOpponentName(opponentId)
                }
            } else if (!isHostOfRoom && opponentId.isNotEmpty() && opponentId != observingOpponentId) {
                // Custom-word / lobby guest: fetch host name and observe host presence
                observingOpponentId = opponentId
                val hostCustomName = room.guestProfiles[opponentId]?.get("name")?.takeIf { it.isNotBlank() }
                if (hostCustomName == null) fetchOpponentName(opponentId)
                observeOpponentPresence(roomId, opponentId)
            }

            // ── Custom-word / lobby guest: observe all other guests' progress ──
            if (isMultiPlayer && !isHostOfRoom) {
                val newOtherGuests = room.guestIds.filter { it != myId && it !in observingGuestIds }
                for (guestId in newOtherGuests) {
                    observingGuestIds.add(guestId)
                    fetchGuestInfo(guestId)
                    observeGuestState(roomId, guestId)
                    presenceManager.observeAfk(roomId, guestId, viewModelScope) { id, isAfk ->
                        updatePlayerAfkState(id, isAfk)
                    }
                }
            }

            // isLobbyMode guests also observe the host's board so it appears in mini-boards
            if (isLobbyModeRoom && !isHostOfRoom && room.hostId.isNotEmpty()
                && room.hostId !in observingGuestIds) {
                observingGuestIds.add(room.hostId)
                observeGuestState(roomId, room.hostId)
                fetchGuestInfo(room.hostId)
            }

            // ── Game-end detection (1v1 non-custom only) ──────────────────────
            if (room.status == "finished" && !uiState.value.isGameOver && !isMultiPlayer) {
                val iWon         = room.winnerId == myId
                val iLeft        = room.leftBy == myId
                val opponentLeft = room.leftBy.isNotEmpty() && room.leftBy != myId
                val opponentFail = room.failedBy.isNotEmpty() && room.failedBy != myId

                setState { copy(isGameOver = true) }
                when {
                    iLeft        -> Unit
                    opponentLeft -> {
                        setState { copy(opponentLeft = true) }
                        sendEffect {
                            MultiplayerGameEffect.ShowGameDialog(
                                isWin = true, targetWord = room.word, opponentLeft = true
                            )
                        }
                    }
                    opponentFail -> {
                        setState { copy(opponentFailed = true) }
                        sendEffect {
                            MultiplayerGameEffect.ShowGameDialog(
                                isWin = true, targetWord = room.word, opponentFailed = true
                            )
                        }
                    }
                    else -> sendEffect {
                        MultiplayerGameEffect.ShowGameDialog(isWin = iWon, targetWord = room.word)
                    }
                }
            }

            // ── Custom-word host left → notify guests regardless of their game-over state ──
            if (room.status == "finished" && isMultiPlayer && !isHostOfRoom && !uiState.value.isHostLeft) {
                setState { copy(isHostLeft = true) }
                sendEffect { MultiplayerGameEffect.HostLeftRoom }
            }

            // Lobby mode: winner broadcast — force ALL players to game-over via room.winnerId
            if (isLobbyModeRoom && !room.winnerId.isNullOrEmpty()) {
                val iWon = room.winnerId == myId
                if (!uiState.value.isGameOver) {
                    val winnerProgress = uiState.value.opponentsProgress[room.winnerId]
                    val winnerName = if (iWon) "" else (winnerProgress?.name?.takeIf { it.isNotBlank() } ?: "")
                    setState { copy(isGameOver = true, isMyWin = iWon, lobbyWinnerName = winnerName) }
                } else if (!iWon && uiState.value.lobbyWinnerName.isBlank()) {
                    // Host already lost before anyone won; record the winner's name now so
                    // the Play Again button becomes enabled.
                    val name = room.guestProfiles[room.winnerId]?.get("name")?.takeIf { it.isNotBlank() }
                        ?: uiState.value.opponentsProgress[room.winnerId]?.name?.takeIf { it.isNotBlank() }
                        ?: room.winnerId!!.take(8)  // fallback: partial ID — non-blank, enables button
                    setState { copy(lobbyWinnerName = name) }
                }
            }

            if (room.status == "finished" && uiState.value.isGameOver && !isMultiPlayer) {
                val opponentJustLeft = room.leftBy.isNotEmpty()
                        && room.leftBy != myId
                        && !uiState.value.opponentLeft
                if (opponentJustLeft) setState { copy(opponentLeft = true) }
            }

            if (room.status == "playing" && uiState.value.isGameOver && !isMultiPlayer) {
                setState {
                    copy(
                        isGameOver     = false,
                        opponentLeft   = false,
                        opponentFailed = false,
                        currentRow     = 0,
                        currentCol     = 0,
                        board          = List(board.size) { List(room.wordLength) { Tile() } },
                        keyboardStates = emptyMap(),
                        targetWord     = room.word.uppercase(),
                    )
                }
                sendEffect { MultiplayerGameEffect.DismissResultDialog }
            }

            if (room.status == "restarting" && uiState.value.isGameOver) {
                sendEffect { MultiplayerGameEffect.DismissResultDialog }
            }

            // Lobby mode: new round started → reset board for ALL players (host + guests)
            if (room.status == "playing" && isLobbyModeRoom && uiState.value.isGameOver
                && room.word.isNotEmpty() && room.word.uppercase() != previousWord) {
                setState {
                    copy(
                        isGameOver        = false,
                        isMyWin           = false,
                        lobbyWinnerName   = "",
                        currentRow        = 0,
                        currentCol        = 0,
                        board             = List(board.size) { List(room.wordLength) { Tile() } },
                        keyboardStates    = emptyMap(),
                        targetWord        = room.word.uppercase(),
                        roundNumber       = room.roundNumber,
                        opponentsProgress = opponentsProgress.mapValues { (_, p) ->
                            p.copy(solved = false, failed = false, guessCount = 0, guessRows = List(MAX_GUESSES) { GuessRow() })
                        },
                    )
                }
            }

            // Custom-word guest: host started a NEW round (word changed) → reset board
            if (room.status == "playing" && isCustomWordRoom && !isLobbyModeRoom && !isHostOfRoom && uiState.value.isGameOver
                && room.word.isNotEmpty() && room.word.uppercase() != previousWord) {
                setState {
                    copy(
                        isGameOver     = false,
                        isMyWin        = false,
                        currentRow     = 0,
                        currentCol     = 0,
                        board          = List(board.size) { List(room.wordLength) { Tile() } },
                        keyboardStates = emptyMap(),
                        targetWord     = room.word.uppercase(),
                        roundNumber    = room.roundNumber,
                        totalPoints    = room.totalPoints,
                    )
                }
                sendEffect { MultiplayerGameEffect.DismissResultDialog }
            }
        }.launchIn(viewModelScope)
    }

    // ── Observe a single guest's player state (host-only, custom word) ────────
    private fun observeGuestState(roomId: String, guestId: String) {
        observeOpponentUseCase(roomId, guestId).onEach { playerState ->
            val s = uiState.value
            val current = s.opponentsProgress[guestId] ?: OpponentProgress()
            val wordLen = s.wordLength.takeIf { it > 0 } ?: 4
            val updated = current.copy(
                solved      = playerState?.solved == true,
                failed      = playerState?.finishedAt != null && playerState.solved != true,
                guessCount  = playerState?.currentRow ?: current.guessCount,
                guessRows   = playerState?.toGuessRows(wordLen) ?: List(MAX_GUESSES) { GuessRow() },
                totalPoints = if (playerState?.solved != true)
                    s.sessionPoints[guestId] ?: current.totalPoints
                else
                    current.totalPoints,
            )
            val updatedProgress = s.opponentsProgress + (guestId to updated)
            setState { copy(opponentsProgress = updatedProgress) }

            // Guest: another player just won → force this player to the lobby as a loss
            if (!s.isHost && !s.isGameOver && updated.solved) {
                setState { copy(isGameOver = true, isMyWin = false, lobbyWinnerName = if (s.isLobbyMode) updated.name else "") }
            }

            // Host in lobby mode: a guest won → host is also game over as a loss
            if (s.isHost && s.isLobbyMode && !s.isGameOver && updated.solved) {
                setState { copy(isGameOver = true, isMyWin = false, lobbyWinnerName = updated.name) }
            }

            // Host in lobby mode: safety net — set isGameOver as soon as the host has used all
            // their guesses, without waiting for other players to finish
            if (s.isHost && s.isLobbyMode && !s.isGameOver && s.currentRow >= s.board.size) {
                setState { copy(isGameOver = true) }
            }

            // Custom-word host: show result sheet as soon as someone wins, or when all have finished
            if (s.isHost && !s.isGameOver && s.guestIds.isNotEmpty() && !s.isLobbyMode) {
                val anyoneSolved = updatedProgress.values.any { it.solved }
                val allDone = s.guestIds.all { id ->
                    val p = updatedProgress[id]
                    p != null && (p.solved || p.failed)
                }
                if (anyoneSolved || allDone) {
                    val winnerName = updatedProgress.entries
                        .firstOrNull { it.value.solved }
                        ?.let { (id, p) -> p.name.takeIf { it.isNotBlank() } ?: guestNameFromId(id) }
                        ?: ""
                    // Mark any still-waiting player as failed
                    val finalProgress = updatedProgress.mapValues { (_, p) ->
                        if (!p.solved && !p.failed) p.copy(failed = true) else p
                    }
                    // Accumulate session points for this round
                    val newSessionPts = s.sessionPoints.toMutableMap()
                    finalProgress.forEach { (guestId, p) ->
                        val pts = if (p.solved) when (p.guessCount) {
                            1    -> 100
                            2    -> 80
                            3    -> 60
                            4    -> 40
                            5    -> 20
                            else -> 10
                        } else 0
                        if (pts > 0) newSessionPts[guestId] = (newSessionPts[guestId] ?: 0) + pts
                    }
                    setState { copy(isGameOver = true, opponentsProgress = finalProgress, sessionPoints = newSessionPts) }
                    viewModelScope.launch {
                        resultHandler.updateSessionPoints(s.roomId, newSessionPts)
                    }
                    sendEffect {
                        MultiplayerGameEffect.ShowGameDialog(
                            isWin       = anyoneSolved,
                            targetWord  = s.targetWord,
                            winnerName  = winnerName,
                            totalPoints = newSessionPts,
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    // ── Fetch guest name/avatar for the waiting room list ─────────────────────
    private fun fetchGuestInfo(guestId: String) {
        if (guestId.startsWith("guest_")) {
            updateGuestInfo(guestId, guestNameFromId(guestId), null)
            return
        }
        viewModelScope.launch {
            val (name, avatarUrl) = profileLoader.fetchProfile(guestId)
            updateGuestInfo(guestId, name ?: guestNameFromId(guestId), avatarUrl)
        }
    }

    private fun updateGuestInfo(guestId: String, name: String, avatarUrl: String?) {
        setState {
            // Prefer any profile override the player has already saved (picked up via room observer)
            val existing = waitingPlayers.firstOrNull { it.userId == guestId }
            val resolvedName = existing?.name?.takeIf { it.isNotBlank() } ?: name
            val resolvedColor = existing?.avatarColor
            val resolvedEmoji = existing?.avatarEmoji
            // Prefer Firestore-sourced avatarUrl already in state over the Strapi one (may be more recent)
            val resolvedUrl = existing?.avatarUrl ?: avatarUrl
            val progress = opponentsProgress[guestId] ?: OpponentProgress()
            copy(
                opponentsProgress = opponentsProgress + (guestId to progress.copy(
                    name        = resolvedName,
                    avatarUrl   = resolvedUrl,
                    avatarColor = resolvedColor,
                    avatarEmoji = resolvedEmoji,
                )),
                waitingPlayers    = waitingPlayers.filter { it.userId != guestId } +
                        WaitingPlayer(guestId, resolvedName, resolvedUrl, resolvedColor, resolvedEmoji, existing?.isReady ?: false),
            )
        }
    }

    private val similarPairs: List<Set<Char>> = listOf(
        setOf('\u0647', '\u0629') // ه ↔ ة
    )

    private fun areSimilarArabicLetters(a: Char, b: Char): Boolean =
        a != b && similarPairs.any { it.contains(a) && it.contains(b) }

    private fun detectLanguage(word: String): String =
        if (word.any { it in '\u0600'..'\u06FF' }) "ar" else "en"

    private fun guestNameFromId(id: String): String {
        val suffix = if (id.startsWith("guest_"))
            id.removePrefix("guest_").take(5)
        else
            id.take(5)
        return "Guest-${suffix.uppercase()}"
    }

    private fun fetchOpponentName(opponentId: String) {
        if (opponentId.startsWith("guest_")) {
            setState { copy(opponentName = guestNameFromId(opponentId), opponentAvatarUrl = null) }
            return
        }
        setState { copy(isOpponentProfileLoading = true) }
        viewModelScope.launch {
            val (profileName, avatarUrl) = profileLoader.fetchProfile(opponentId, forceRefresh = false)
            val isRealName = profileName != null && profileName != opponentId
            val name = if (isRealName) profileName else null
            setState {
                copy(
                    opponentName             = name ?: guestNameFromId(opponentId),
                    opponentAvatarUrl        = if (name != null) avatarUrl else null,
                    isOpponentProfileLoading = false,
                )
            }
        }
    }

    private fun observeOpponent(roomId: String, opponentId: String) {
        observeOpponentUseCase(roomId, opponentId).onEach { state ->
            setState { copy(opponentState = state) }
        }.launchIn(viewModelScope)
    }

    private fun observeOpponentPresence(roomId: String, userId: String) {
        presenceManager.observe(roomId, userId, viewModelScope) { droppedId ->
            val s = uiState.value
            if (s.isGameOver) return@observe
            when {
                (s.isCustomWord || s.isLobbyMode) && s.isHost -> {
                    runCatching { removeGuestFromRoomUseCase(s.roomId, droppedId) }
                }
                (s.isCustomWord || s.isLobbyMode) && !s.isHost -> {
                    if (droppedId == s.opponentId && !s.isHostLeft) {
                        setState { copy(isHostLeft = true) }
                        sendEffect { MultiplayerGameEffect.HostLeftRoom }
                    }
                }
                else -> sendEffect { MultiplayerGameEffect.OpponentDisconnected }
            }
        }
        presenceManager.observeAfk(roomId, userId, viewModelScope) { id, isAfk ->
            updatePlayerAfkState(id, isAfk)
        }
    }

    private fun updatePlayerAfkState(userId: String, isAfk: Boolean) {
        if (!isAfk) {
            // Player came back — cancel countdown and clear AFK state
            countdownJobs[userId]?.cancel()
            countdownJobs.remove(userId)
            setState {
                val updatedProgress = opponentsProgress[userId]?.let { c ->
                    opponentsProgress + (userId to c.copy(isAfk = false, afkCountdown = null))
                } ?: opponentsProgress
                val updatedWaiting = waitingPlayers.map { p ->
                    if (p.userId == userId) p.copy(isAfk = false, afkCountdown = null) else p
                }
                copy(
                    opponentsProgress    = updatedProgress,
                    waitingPlayers       = updatedWaiting,
                    isOpponentAfk        = if (userId == opponentId) false else isOpponentAfk,
                    opponentAfkCountdown = if (userId == opponentId) null else opponentAfkCountdown,
                )
            }
            return
        }

        // isAfk = true — set AFK state immediately
        setState {
            val updatedProgress = opponentsProgress[userId]?.let { c ->
                opponentsProgress + (userId to c.copy(isAfk = true))
            } ?: opponentsProgress
            val updatedWaiting = waitingPlayers.map { p ->
                if (p.userId == userId) p.copy(isAfk = true) else p
            }
            copy(
                opponentsProgress = updatedProgress,
                waitingPlayers    = updatedWaiting,
                isOpponentAfk     = if (userId == opponentId) true else isOpponentAfk,
            )
        }

        // Don't start a new countdown if one is already running
        if (countdownJobs[userId]?.isActive == true) return

        countdownJobs[userId] = viewModelScope.launch {
            for (secondsLeft in 60 downTo 0) {
                setState {
                    val updatedProgress = opponentsProgress[userId]?.let { c ->
                        opponentsProgress + (userId to c.copy(afkCountdown = secondsLeft))
                    } ?: opponentsProgress
                    val updatedWaiting = waitingPlayers.map { p ->
                        if (p.userId == userId) p.copy(afkCountdown = secondsLeft) else p
                    }
                    copy(
                        opponentsProgress    = updatedProgress,
                        waitingPlayers       = updatedWaiting,
                        opponentAfkCountdown = if (userId == opponentId) secondsLeft else opponentAfkCountdown,
                    )
                }
                if (secondsLeft == 0) break
                delay(1_000L)
            }
            // Countdown reached 0 — remove player
            countdownJobs.remove(userId)
            val s = uiState.value
            if (!s.isGameOver && s.roomId.isNotEmpty()) {
                when {
                    (s.isCustomWord || s.isLobbyMode) && s.isHost ->
                        runCatching { removeGuestFromRoomUseCase(s.roomId, userId) }
                    (s.isCustomWord || s.isLobbyMode) && !s.isHost -> {
                        if (userId == s.opponentId && !s.isHostLeft) {
                            setState { copy(isHostLeft = true) }
                            sendEffect { MultiplayerGameEffect.HostLeftRoom }
                        }
                        // co-guest dropped — host handles their removal via removeGuestFromRoomUseCase
                    }
                    else -> sendEffect { MultiplayerGameEffect.OpponentDisconnected }
                }
            }
        }
    }

    // ── Start match (host only) ───────────────────────────────────────────────
    private fun startMatch() {
        val s = uiState.value
        if (s.isLobbyMode && !s.isCustomWord) {
            viewModelScope.launch {
                val length = listOf(4, 5, 6).random()
                val words = wordCache[length] ?: run {
                    val result = getWordsUseCase(s.language, length)
                    if (result is Resource.Success && result.data.isNotEmpty()) {
                        wordCache[length] = result.data.filter { it.length == length }
                        result.data.filter { it.length == length }
                    } else null
                }
                val word = words?.randomOrNull()?.uppercase() ?: return@launch
                restartRoomUseCase(s.roomId, word, word.length, 1, emptyMap())
            }
        } else {
            viewModelScope.launch { startRoomUseCase(uiState.value.roomId) }
        }
    }

    private fun startMatchWithWord(word: String) {
        val s = uiState.value
        val w = word.uppercase().trim()
        if (w.isEmpty()) return
        viewModelScope.launch {
            restartRoomUseCase(s.roomId, w, w.length, roundNumber = 1, totalPoints = emptyMap())
        }
    }

    // ── Leave match ───────────────────────────────────────────────────────────
    private fun leaveMatch() {
        val s = uiState.value
        // Navigate back immediately so the button always works, even when offline.
        // Firebase cleanup runs as best-effort in the background.
        sendEffect { MultiplayerGameEffect.NavigateBack }
        if (s.roomId.isEmpty() || s.myUserId.isEmpty()) return
        viewModelScope.launch {
            if (s.isCustomWord || s.isLobbyMode) {
                if (s.isHost) {
                    // Host ends the session for everyone
                    runCatching { leaveRoomUseCase(s.roomId, s.myUserId) }
                } else {
                    // Guest leaving at any point: remove from guestIds and clear their vote
                    runCatching { removeGuestFromRoomUseCase(s.roomId, s.myUserId) }
                }
            } else {
                runCatching { leaveRoomUseCase(s.roomId, s.myUserId) }
            }
        }
    }

    private fun rejoinRoom() {
        val s = uiState.value
        if (s.roomId.isEmpty() || s.myUserId.isEmpty()) return
        viewModelScope.launch {
            runCatching { addGuestToRoomUseCase(s.roomId, s.myUserId) }
        }
    }

    private fun enterLetter(letter: Char) {
        val s = uiState.value
        if (s.currentCol >= s.wordLength || s.isGameOver) return

        val newBoard = s.board.mapIndexed { r, row ->
            if (r == s.currentRow) row.mapIndexed { c, tile ->
                if (c == s.currentCol) tile.copy(letter = letter, state = TileState.FILLED) else tile
            } else row
        }
        val newCol = s.currentCol + 1
        setState { copy(board = newBoard, currentCol = newCol) }
        if (newCol == s.wordLength && !s.isGameOver) submitGuess(forceCol = newCol)
    }

    private fun deleteLetter() {
        val s = uiState.value
        if (s.currentCol == 0) return
        val newCol = s.currentCol - 1
        val newBoard = s.board.mapIndexed { r, row ->
            if (r == s.currentRow) row.mapIndexed { c, tile ->
                if (c == newCol) tile.copy(letter = ' ', state = TileState.EMPTY) else tile
            } else row
        }
        setState { copy(board = newBoard, currentCol = newCol) }
    }

    private fun submitGuess(forceCol: Int? = null) {
        val s = uiState.value
        val col = forceCol ?: s.currentCol
        if (col < s.wordLength || s.isGameOver) return

        val rawGuess = s.board[s.currentRow]
            .filter { it.letter != ' ' }
            .joinToString("") { it.letter.toString() }

        viewModelScope.launch {
            val isTargetWord = rawGuess.normalizeForWordle() == s.targetWord.normalizeForWordle()
            if (!isTargetWord) {
                val wordList = wordCache[s.wordLength] ?: emptyList()
                val isValid = validateWordUseCase(rawGuess, s.language, wordList)
                if (!isValid) {
                    sendEffect { MultiplayerGameEffect.NotInWordList }
                    return@launch
                }
            }

            val s2     = uiState.value
            val guess  = s2.board[s2.currentRow].filter { it.letter != ' ' }.map { it.letter }
            val target = s2.targetWord.uppercase().toList()
            if (guess.size != s2.wordLength || target.size != s2.wordLength) return@launch

            // Two-pass evaluation matching single-player logic (supports SIMILAR)
            val tileStates   = Array(s2.wordLength) { TileState.WRONG }
            val remainingTarget = target.toMutableList()

            // First pass: exact matches and similar-letter matches
            for (i in guess.indices) {
                when {
                    guess[i] == target[i] -> {
                        tileStates[i]      = TileState.CORRECT
                        remainingTarget[i] = '\u0000'
                    }
                    areSimilarArabicLetters(guess[i], target[i]) -> {
                        tileStates[i]      = TileState.SIMILAR
                        remainingTarget[i] = '\u0000'
                    }
                }
            }
            // Second pass: misplaced letters
            for (i in guess.indices) {
                if (tileStates[i] == TileState.CORRECT || tileStates[i] == TileState.SIMILAR) continue
                val idx = remainingTarget.indexOf(guess[i])
                if (idx != -1) {
                    tileStates[i]      = TileState.MISPLACED
                    remainingTarget[idx] = '\u0000'
                }
            }

            val newBoard = s2.board.mapIndexed { r, row ->
                if (r == s2.currentRow) row.mapIndexed { c, tile ->
                    tile.copy(state = tileStates.getOrElse(c) { TileState.EMPTY })
                } else row
            }

            val priority = mapOf(
                TileState.CORRECT   to 5,
                TileState.SIMILAR   to 4,
                TileState.MISPLACED to 3,
                TileState.WRONG     to 2,
                TileState.FILLED    to 1,
                TileState.EMPTY     to 0,
            )
            val newKeyboardStates = s2.keyboardStates.toMutableMap()
            guess.forEachIndexed { i, ch ->
                val incoming = tileStates[i]
                val current  = newKeyboardStates[ch]
                if ((priority[incoming] ?: 0) > (priority[current] ?: 0)) {
                    newKeyboardStates[ch] = incoming
                }
            }

            val solved   = tileStates.all { it == TileState.CORRECT || it == TileState.SIMILAR }
            val newRow   = s2.currentRow + 1
            val gameOver = solved || newRow >= newBoard.size
            if (gameStartTime == 0L) gameStartTime = System.currentTimeMillis()

            setState {
                copy(board = newBoard, currentRow = newRow, currentCol = 0, keyboardStates = newKeyboardStates)
            }

            val allGuesses = newBoard.take(newRow).map { row ->
                row.filter { it.letter != ' ' }.joinToString(",") { it.letter.toString() }
            }
            val allTypes = newBoard.take(newRow).map { row ->
                row.joinToString(",") { tile ->
                    when (tile.state) {
                        TileState.CORRECT   -> "CORRECT"
                        TileState.SIMILAR   -> "SIMILAR"
                        TileState.MISPLACED -> "PRESENT"
                        TileState.WRONG     -> "ABSENT"
                        else                -> "DEFAULT"
                    }
                }
            }

            updatePlayerStateUseCase(
                roomId = s2.roomId,
                userId = s2.myUserId,
                state  = PlayerState(
                    guesses    = allGuesses,
                    types      = allTypes,
                    currentRow = newRow,
                    currentCol = 0,
                    solved     = solved,
                    finishedAt = if (gameOver) System.currentTimeMillis() else null
                )
            )

            if (gameOver) {
                val elapsed = (System.currentTimeMillis() - gameStartTime) / 1000L
                gameStartTime = 0L  // reset for next round
                viewModelScope.launch {
                    resultHandler.evaluateAndAward(
                        GameResult(
                            isWin            = solved,
                            guessCount       = newRow,
                            timeTakenSeconds = elapsed,
                            wordLength       = s2.wordLength,
                            gameMode         = GameMode.MULTIPLAYER,
                            language         = s2.language,
                        )
                    )
                }
                if (s2.isCustomWord || s2.isLobbyMode) {
                    // ── Custom word / lobby mode: go to in-screen result lobby ─
                    setState { copy(isGameOver = true, isMyWin = solved) }
                    if (solved && s2.isLobbyMode) {
                        // Award points to the winner and broadcast to all players via room.winnerId
                        val pts = when (newRow) { 1 -> 100; 2 -> 80; 3 -> 60; 4 -> 40; 5 -> 20; else -> 10 }
                        val newSessionPts = s2.sessionPoints.toMutableMap()
                        newSessionPts[s2.myUserId] = (newSessionPts[s2.myUserId] ?: 0) + pts
                        setState { copy(sessionPoints = newSessionPts) }
                        viewModelScope.launch {
                            resultHandler.updateSessionPoints(s2.roomId, newSessionPts)
                            resultHandler.setLobbyWinner(s2.roomId, s2.myUserId)
                        }
                    }
                } else {
                    // ── 1v1 non-custom: finish room as before ─────────────────
                    if (solved) {
                        finishRoomUseCase(s2.roomId, s2.myUserId)
                    } else {
                        val winner = s2.opponentId.takeIf { it.isNotEmpty() } ?: ""
                        finishRoomUseCase(s2.roomId, winner, failedBy = s2.myUserId)
                    }
                }
            }
        }
    }

    private fun votePlayAgain() {
        val s = uiState.value
        viewModelScope.launch {
            if (s.myUserId in s.playAgainVotes) {
                resultHandler.unvote(s.roomId, s.myUserId)
            } else {
                resultHandler.vote(s.roomId, s.myUserId)
            }
        }
    }

    private fun playAgainLobbyMode() {
        val s = uiState.value
        if (!s.isHost || !s.isLobbyMode) return
        viewModelScope.launch {
            val length = listOf(4, 5, 6).random()
            val words = wordCache[length] ?: run {
                val result = getWordsUseCase(s.language, length)
                if (result is Resource.Success && result.data.isNotEmpty()) {
                    wordCache[length] = result.data; result.data
                } else null
            }
            val word = words?.randomOrNull()?.uppercase() ?: return@launch
            val newRound = s.roundNumber + 1
            // Clear all players' states so boards start fresh
            updatePlayerStateUseCase(s.roomId, s.myUserId, PlayerState())
            s.guestIds.forEach { guestId ->
                updatePlayerStateUseCase(s.roomId, guestId, PlayerState())
            }
            restartRoomUseCase(s.roomId, word, word.length, newRound, s.sessionPoints)
        }
    }

    private fun updateGuestProfile(name: String, avatarColor: Long?, avatarEmoji: String?) {
        val trimmed = name.trim().ifBlank { uiState.value.myName }
        setState { copy(myName = trimmed, avatarColor = avatarColor, avatarEmoji = avatarEmoji) }
        val s = uiState.value
        viewModelScope.launch {
            // Persist locally so the profile survives app restarts
            profileLoader.saveGuestProfile(trimmed, avatarColor, avatarEmoji)
            // Push to Firestore so other players see the updated name/avatar
            if (s.roomId.isNotEmpty() && s.myUserId.isNotEmpty()) {
                runCatching { updateGuestProfileUseCase(s.roomId, s.myUserId, trimmed, avatarColor, avatarEmoji) }
            }
        }
    }

    private fun playAgainCustomWord(newWord: String) {
        val s = uiState.value
        val word = newWord.uppercase().trim()
        if (word.isEmpty()) return

        val newRound = s.roundNumber + 1

        setState {
            copy(
                isGameOver    = false,
                targetWord    = word,
                roundNumber   = newRound,
                opponentsProgress = opponentsProgress.mapValues { (guestId, p) ->
                    p.copy(
                        solved      = false,
                        failed      = false,
                        guessCount  = 0,
                        guessRows   = List(MAX_GUESSES) { GuessRow() },
                        totalPoints = sessionPoints[guestId] ?: p.totalPoints,
                    )
                },
            )
        }
        viewModelScope.launch {
            s.guestIds.forEach { guestId ->
                updatePlayerStateUseCase(s.roomId, guestId, PlayerState())
            }
            restartRoomUseCase(s.roomId, word, word.length, newRound, s.sessionPoints)
        }
    }

    private fun restartGame() {
        val s = uiState.value
        setState {
            copy(
                currentRow = 0, currentCol = 0,
                board = List(board.size) { List(wordLength) { Tile() } },
                keyboardStates = emptyMap(), isGameOver = false,
            )
        }
        viewModelScope.launch {
            val claimed = try { restartRoomUseCase.claimRestart(s.roomId); true }
            catch (_: Exception) { false }
            if (!claimed) return@launch

            updatePlayerStateUseCase(s.roomId, s.myUserId, PlayerState())
            if (s.opponentId.isNotEmpty()) updatePlayerStateUseCase(s.roomId, s.opponentId, PlayerState())

            val newWordLength = listOf(4, 5, 6).random()
            val cached = wordCache[newWordLength]
            val words = if (cached != null) cached else {
                val result = getWordsUseCase(s.language, newWordLength)
                val fetched = if (result is Resource.Success) result.data else null
                if (fetched != null) wordCache[newWordLength] = fetched
                fetched
            }
            val newWord = words?.randomOrNull() ?: return@launch
            restartRoomUseCase(s.roomId, newWord.uppercase(), newWordLength)
        }
    }
}
