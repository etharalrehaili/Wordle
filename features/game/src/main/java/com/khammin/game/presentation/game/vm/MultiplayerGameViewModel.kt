package com.khammin.game.presentation.game.vm

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.domain.model.PlayerState
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.presentation.components.GuessRow
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.enums.TileState
import com.khammin.core.presentation.components.enums.Types
import com.khammin.core.presentation.components.toGuessRows
import com.khammin.core.util.Resource
import com.khammin.core.util.normalizeForWordle
import com.khammin.game.domain.usecases.game.FinishRoomUseCase
import com.khammin.game.domain.usecases.game.GetWordsUseCase
import com.khammin.game.domain.usecases.game.LeaveRoomUseCase
import com.khammin.game.domain.usecases.game.ObserveOpponentPresenceUseCase
import com.khammin.game.domain.usecases.game.ObserveOpponentUseCase
import com.khammin.game.domain.usecases.game.ObserveRoomUseCase
import com.khammin.game.domain.usecases.game.RegisterPresenceUseCase
import com.khammin.game.domain.usecases.game.RestartRoomUseCase
import com.khammin.game.domain.usecases.game.RemoveGuestFromRoomUseCase
import com.khammin.game.domain.usecases.game.ResetCustomRoomUseCase
import com.khammin.game.domain.usecases.game.StartRoomUseCase
import com.khammin.game.domain.usecases.game.UpdateGuestProfileUseCase
import com.khammin.game.domain.usecases.game.UpdateSessionPointsUseCase
import com.khammin.game.domain.usecases.game.VotePlayAgainUseCase
import com.khammin.game.domain.usecases.game.UpdatePlayerStateUseCase
import com.khammin.game.domain.usecases.game.ValidateWordUseCase
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import com.khammin.game.presentation.game.contract.MultiplayerGameEffect
import com.khammin.game.presentation.game.contract.MultiplayerGameIntent
import com.khammin.game.presentation.game.contract.MultiplayerGameUiState
import com.khammin.game.presentation.game.contract.OpponentProgress
import com.khammin.game.presentation.game.contract.Tile
import com.khammin.game.presentation.game.contract.WaitingPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MultiplayerGameViewModel @Inject constructor(
    private val observeRoomUseCase: ObserveRoomUseCase,
    private val observeOpponentUseCase: ObserveOpponentUseCase,
    private val updatePlayerStateUseCase: UpdatePlayerStateUseCase,
    private val finishRoomUseCase: FinishRoomUseCase,
    private val leaveRoomUseCase: LeaveRoomUseCase,
    private val restartRoomUseCase: RestartRoomUseCase,
    private val getWordsUseCase: GetWordsUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val registerPresenceUseCase: RegisterPresenceUseCase,
    private val observeOpponentPresenceUseCase: ObserveOpponentPresenceUseCase,
    private val validateWordUseCase: ValidateWordUseCase,
    private val startRoomUseCase: StartRoomUseCase,
    private val removeGuestFromRoomUseCase: RemoveGuestFromRoomUseCase,
    private val resetCustomRoomUseCase: ResetCustomRoomUseCase,
    private val votePlayAgainUseCase: VotePlayAgainUseCase,
    private val updateGuestProfileUseCase: UpdateGuestProfileUseCase,
    private val updateSessionPointsUseCase: UpdateSessionPointsUseCase,
    private val auth: FirebaseAuth
) : BaseMviViewModel<MultiplayerGameIntent, MultiplayerGameUiState, MultiplayerGameEffect>(
    initialState = MultiplayerGameUiState()
) {

    override fun onEvent(intent: MultiplayerGameIntent) {
        when (intent) {
            is MultiplayerGameIntent.LoadGame -> loadGame(
                intent.roomId, intent.language, intent.isHost, intent.myUserId,
                intent.isCustomWord, intent.defaultMyName, intent.defaultGuestName
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
            is MultiplayerGameIntent.UpdateGuestProfile  -> updateGuestProfile(intent.name, intent.avatarColor, intent.avatarEmoji)
        }
    }

    private var observingOpponentId: String = ""
    private val observingGuestIds = mutableSetOf<String>()
    private val wordCache: MutableMap<Int, List<String>> = mutableMapOf()
    private var presenceStarted = false

    private fun loadGame(
        roomId: String,
        language: String,
        isHost: Boolean,
        myUserId: String,
        isCustomWord: Boolean,
        defaultMyName: String,
        defaultGuestName: String,
    ) {
        val myId = myUserId.takeIf { it.isNotEmpty() }
            ?: auth.currentUser?.uid
            ?: uiState.value.myUserId.takeIf { it.isNotEmpty() }
            ?: return

        val isAnonymous = auth.currentUser?.isAnonymous == true || myId.startsWith("guest_")
        val initialMyName = if (isAnonymous) guestNameFromId(myId) else defaultMyName

        setState {
            copy(
                roomId = roomId, myUserId = myId, isHost = isHost,
                isCustomWord = isCustomWord, language = language,
                defaultMyName = defaultMyName, defaultGuestName = defaultGuestName,
                myName = initialMyName,
                isAnonymous = isAnonymous,
            )
        }

        viewModelScope.launch { registerPresenceUseCase(roomId, myId) }

        viewModelScope.launch {
            if (!isAnonymous) {
                val result = getProfileUseCase(myId)
                if (result is Resource.Success) {
                    val name = result.data?.name?.takeIf { it.isNotBlank() } ?: defaultMyName
                    setState { copy(myName = name) }
                }
            }
        }

        listOf(4, 5, 6).forEach { length ->
            viewModelScope.launch {
                val result = getWordsUseCase(language, length)
                if (result is Resource.Success && result.data.isNotEmpty()) {
                    wordCache[length] = result.data
                }
            }
        }

        observeRoomUseCase(roomId).onEach { room ->
            if (room == null) return@onEach

            val isCustomWordRoom = isCustomWord || room.isCustomWord
            val isHostOfRoom = room.hostId == myId
            val previousWord     = uiState.value.targetWord
            val previousGuestIds = uiState.value.guestIds

            // Compute opponent ID for 1v1 (non-custom) or custom-word guest
            val opponentId = when {
                isHostOfRoom && !isCustomWordRoom -> room.guestId
                isHostOfRoom                     -> ""          // host has many opponents
                else                             -> room.hostId // guest always sees host
            }

            setState {
                val boardResized = wordLength != room.wordLength
                // Rebuild waitingPlayers from room.guestIds so late-joining players immediately
                // get their saved profile (name/avatar) from guestProfiles.
                val updatedWaiting = room.guestIds.map { guestId ->
                    val existing = waitingPlayers.firstOrNull { it.userId == guestId }
                    val profile  = room.guestProfiles[guestId]
                    when {
                        profile != null -> (existing ?: WaitingPlayer(guestId, guestNameFromId(guestId))).copy(
                            name        = profile["name"]?.takeIf { it.isNotBlank() } ?: existing?.name ?: guestNameFromId(guestId),
                            avatarColor = profile["avatarColor"]?.toLongOrNull() ?: existing?.avatarColor,
                            avatarEmoji = profile["avatarEmoji"]?.takeIf { it.isNotEmpty() } ?: existing?.avatarEmoji,
                        )
                        existing != null -> existing
                        else -> WaitingPlayer(guestId, guestNameFromId(guestId)) // placeholder until fetchGuestInfo fills it in
                    }
                }
                val updatedProgress = opponentsProgress
                    .filter { it.key in room.guestIds }
                    .mapValues { (guestId, progress) ->
                        val profile = room.guestProfiles[guestId]
                        if (profile != null) progress.copy(
                            name        = profile["name"]?.takeIf { it.isNotBlank() } ?: progress.name,
                            avatarColor = profile["avatarColor"]?.toLongOrNull() ?: progress.avatarColor,
                            avatarEmoji = profile["avatarEmoji"]?.takeIf { it.isNotEmpty() } ?: progress.avatarEmoji,
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
                copy(
                    targetWord            = room.word.uppercase(),
                    wordLength            = room.wordLength,
                    language              = if (room.word.isNotEmpty()) detectLanguage(room.word) else language,
                    opponentId            = opponentId,
                    isHost                = isHostOfRoom,
                    isCustomWord          = isCustomWordRoom,
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
                )
            }

            // Remove departed guests from the tracking set so they are re-observed when they rejoin
            observingGuestIds.retainAll(room.guestIds.toSet())

            // Host: all guests left during a live game
            if (isHostOfRoom && isCustomWordRoom && room.status == "playing"
                && room.guestIds.isEmpty() && previousGuestIds.isNotEmpty()) {
                sendEffect { MultiplayerGameEffect.AllPlayersLeft }
            }

            if (room.wordLength > 0 && !wordCache.containsKey(room.wordLength)) {
                viewModelScope.launch {
                    val result = getWordsUseCase(language, room.wordLength)
                    if (result is Resource.Success && result.data.isNotEmpty()) {
                        wordCache[room.wordLength] = result.data
                    }
                }
            }

            // ── Host of custom-word room: observe each new guest ──────────────
            if (isCustomWordRoom && isHostOfRoom) {
                val newGuests = room.guestIds.filter { it !in observingGuestIds }
                for (guestId in newGuests) {
                    observingGuestIds.add(guestId)
                    observeGuestState(roomId, guestId)
                    fetchGuestInfo(guestId)
                }
            }

            // ── 1v1 / custom-word guest: observe host name + presence ─────────
            if (!isCustomWordRoom) {
                if (opponentId.isNotEmpty() && opponentId != observingOpponentId) {
                    observingOpponentId = opponentId
                    observeOpponent(roomId, opponentId)
                    fetchOpponentName(opponentId)
                    observeOpponentPresence(roomId, opponentId)
                }
            } else if (!isHostOfRoom && opponentId.isNotEmpty() && opponentId != observingOpponentId) {
                // Custom-word guest: fetch host name only if no custom name is saved yet
                observingOpponentId = opponentId
                val hostCustomName = room.guestProfiles[opponentId]?.get("name")?.takeIf { it.isNotBlank() }
                if (hostCustomName == null) fetchOpponentName(opponentId)
                // else: resolvedOpponentName above already applied the custom name
            }

            // ── Custom-word guest: observe other guests' progress for game-over lobby ──
            if (isCustomWordRoom && !isHostOfRoom) {
                val newOtherGuests = room.guestIds.filter { it != myId && it !in observingGuestIds }
                for (guestId in newOtherGuests) {
                    observingGuestIds.add(guestId)
                    fetchGuestInfo(guestId)
                    observeGuestState(roomId, guestId)
                }
            }

            // ── Game-end detection (1v1 non-custom only) ──────────────────────
            if (room.status == "finished" && !uiState.value.isGameOver && !isCustomWordRoom) {
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
            if (room.status == "finished" && isCustomWordRoom && !isHostOfRoom && !uiState.value.isHostLeft) {
                setState { copy(isHostLeft = true) }
                sendEffect { MultiplayerGameEffect.HostLeftRoom }
            }

            if (room.status == "finished" && uiState.value.isGameOver && !isCustomWordRoom) {
                val opponentJustLeft = room.leftBy.isNotEmpty()
                    && room.leftBy != myId
                    && !uiState.value.opponentLeft
                if (opponentJustLeft) setState { copy(opponentLeft = true) }
            }

            if (room.status == "playing" && uiState.value.isGameOver && !isCustomWordRoom) {
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

            // Custom-word guest: host started a NEW round (word changed) → reset board
            if (room.status == "playing" && isCustomWordRoom && !isHostOfRoom && uiState.value.isGameOver
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
                totalPoints = s.sessionPoints[guestId] ?: current.totalPoints,
            )
            val updatedProgress = s.opponentsProgress + (guestId to updated)
            setState { copy(opponentsProgress = updatedProgress) }

            // Guest: another player just won → force this player to the lobby as a loss
            if (!s.isHost && !s.isGameOver && updated.solved) {
                setState { copy(isGameOver = true, isMyWin = false) }
            }

            // Host: show result sheet as soon as someone wins, or when all have finished
            if (s.isHost && !s.isGameOver && s.guestIds.isNotEmpty()) {
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
                        runCatching { updateSessionPointsUseCase(s.roomId, newSessionPts) }
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
            val name = guestNameFromId(guestId)
            updateGuestInfo(guestId, name, null)
            return
        }
        viewModelScope.launch {
            val result = getProfileUseCase(guestId)
            val name = (result as? Resource.Success)?.data?.name?.takeIf { it.isNotBlank() }
                ?: guestNameFromId(guestId)
            val avatar = (result as? Resource.Success)?.data?.avatarUrl
            updateGuestInfo(guestId, name, avatar)
        }
    }

    private fun updateGuestInfo(guestId: String, name: String, avatarUrl: String?) {
        setState {
            // Prefer any profile override the player has already saved (picked up via room observer)
            val existing = waitingPlayers.firstOrNull { it.userId == guestId }
            val resolvedName = existing?.name?.takeIf { it.isNotBlank() } ?: name
            val resolvedColor = existing?.avatarColor
            val resolvedEmoji = existing?.avatarEmoji
            val progress = opponentsProgress[guestId] ?: OpponentProgress()
            copy(
                opponentsProgress = opponentsProgress + (guestId to progress.copy(
                    name        = resolvedName,
                    avatarUrl   = avatarUrl,
                    avatarColor = resolvedColor,
                    avatarEmoji = resolvedEmoji,
                )),
                waitingPlayers    = waitingPlayers.filter { it.userId != guestId } +
                    WaitingPlayer(guestId, resolvedName, avatarUrl, resolvedColor, resolvedEmoji),
            )
        }
    }

    private fun observeOpponentPresence(roomId: String, opponentId: String) {
        observeOpponentPresenceUseCase(roomId, opponentId).onEach { isOnline ->
            if (!presenceStarted) {
                presenceStarted = isOnline
                return@onEach
            }
            if (!isOnline && !uiState.value.isGameOver && !uiState.value.isCustomWord) {
                setState { copy(opponentLeft = true, isGameOver = true) }
                sendEffect {
                    MultiplayerGameEffect.ShowGameDialog(
                        isWin = true, targetWord = uiState.value.targetWord, opponentLeft = true
                    )
                }
                viewModelScope.launch {
                    val s = uiState.value
                    finishRoomUseCase(s.roomId, s.myUserId)
                }
            }
        }.launchIn(viewModelScope)
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
        android.util.Log.d("GuestName", "fetchOpponentName: $opponentId")
        if (opponentId.startsWith("guest_")) {
            setState { copy(opponentName = guestNameFromId(opponentId), opponentAvatarUrl = null) }
            return
        }
        setState { copy(isOpponentProfileLoading = true) }
        viewModelScope.launch {
            val result = getProfileUseCase(opponentId)
            when (result) {
                is Resource.Success -> {
                    val profileName = result.data?.name?.takeIf { it.isNotBlank() }
                    val isRealName  = profileName != null && profileName != opponentId
                    val name        = if (isRealName) profileName else null
                    setState {
                        copy(
                            opponentName             = name ?: guestNameFromId(opponentId),
                            opponentAvatarUrl        = if (name != null) result.data?.avatarUrl else null,
                            isOpponentProfileLoading = false
                        )
                    }
                }
                else -> setState {
                    copy(opponentName = guestNameFromId(opponentId), opponentAvatarUrl = null, isOpponentProfileLoading = false)
                }
            }
        }
    }

    private fun observeOpponent(roomId: String, opponentId: String) {
        observeOpponentUseCase(roomId, opponentId).onEach { state ->
            setState { copy(opponentState = state) }
        }.launchIn(viewModelScope)
    }

    // ── Start match (host only) ───────────────────────────────────────────────
    private fun startMatch() {
        viewModelScope.launch { startRoomUseCase(uiState.value.roomId) }
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
        if (s.roomId.isEmpty() || s.myUserId.isEmpty()) {
            sendEffect { MultiplayerGameEffect.NavigateBack }
            return
        }
        viewModelScope.launch {
            if (s.isCustomWord) {
                if (s.isHost) {
                    // Host ends the session for everyone
                    leaveRoomUseCase(s.roomId, s.myUserId)
                } else {
                    // Guest leaving at any point: remove from guestIds and clear their vote
                    removeGuestFromRoomUseCase(s.roomId, s.myUserId)
                }
            } else {
                leaveRoomUseCase(s.roomId, s.myUserId)
            }
            sendEffect { MultiplayerGameEffect.NavigateBack }
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
                if (s2.isCustomWord) {
                    // ── Custom word guest: go to in-screen result lobby ────────
                    setState { copy(isGameOver = true, isMyWin = solved) }
                    // host will see ShowGameDialog when all guests finish (observeGuestState)
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
                votePlayAgainUseCase.unvote(s.roomId, s.myUserId)
            } else {
                votePlayAgainUseCase.vote(s.roomId, s.myUserId)
            }
        }
    }

    private fun updateGuestProfile(name: String, avatarColor: Long?, avatarEmoji: String?) {
        val trimmed = name.trim().ifBlank { uiState.value.myName }
        setState { copy(myName = trimmed, avatarColor = avatarColor, avatarEmoji = avatarEmoji) }
        val s = uiState.value
        if (s.roomId.isNotEmpty() && s.myUserId.isNotEmpty()) {
            viewModelScope.launch {
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
