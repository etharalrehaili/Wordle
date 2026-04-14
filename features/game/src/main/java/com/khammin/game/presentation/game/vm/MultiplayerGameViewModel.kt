package com.khammin.game.presentation.game.vm

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.domain.model.PlayerState
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.presentation.components.enums.TileState
import com.khammin.core.presentation.components.enums.Types
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
import com.khammin.game.domain.usecases.game.StartRoomUseCase
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
            MultiplayerGameIntent.LeaveMatch     -> leaveMatch()
            MultiplayerGameIntent.RestartGame    -> restartGame()
            MultiplayerGameIntent.StartMatch     -> startMatch()
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
                myName = initialMyName
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

            // Compute opponent ID for 1v1 (non-custom) or custom-word guest
            val opponentId = when {
                isHostOfRoom && !isCustomWordRoom -> room.guestId
                isHostOfRoom                     -> ""          // host has many opponents
                else                             -> room.hostId // guest always sees host
            }

            setState {
                val boardResized = wordLength != room.wordLength
                copy(
                    targetWord        = room.word.uppercase(),
                    wordLength        = room.wordLength,
                    language          = if (room.word.isNotEmpty()) detectLanguage(room.word) else language,
                    opponentId        = opponentId,
                    isHost            = isHostOfRoom,
                    isCustomWord      = isCustomWordRoom,
                    guestIds          = room.guestIds,
                    roomStatus        = room.status,
                    board             = if (boardResized) List(board.size) { List(room.wordLength) { Tile() } } else board,
                    currentRow        = if (boardResized) 0 else currentRow,
                    currentCol        = if (boardResized) 0 else currentCol,
                    waitingPlayers    = waitingPlayers.filter { it.userId in room.guestIds },
                    opponentsProgress = opponentsProgress.filter { it.key in room.guestIds },
                )
            }

            // Remove departed guests from the tracking set so they are re-observed when they rejoin
            observingGuestIds.retainAll(room.guestIds.toSet())

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
                // Custom-word guest: just fetch host name (no presence needed)
                observingOpponentId = opponentId
                fetchOpponentName(opponentId)
            }

            // ── Custom-word guest: fetch names of other guests for lobby view ──
            if (isCustomWordRoom && !isHostOfRoom) {
                val newOtherGuests = room.guestIds.filter { it != myId && it !in observingGuestIds }
                for (guestId in newOtherGuests) {
                    observingGuestIds.add(guestId)
                    fetchGuestInfo(guestId)
                }
            }

            // ── Game-end detection (1v1 non-custom only) ──────────────────────
            if (room.status == "finished" && !uiState.value.isGameOver) {
                if (!isCustomWordRoom) {
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
                } else {
                    // Custom-word room ended (host left) → notify guests
                    if (!isHostOfRoom) {
                        sendEffect { MultiplayerGameEffect.HostLeftRoom }
                    }
                }
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
        }.launchIn(viewModelScope)
    }

    // ── Observe a single guest's player state (host-only, custom word) ────────
    private fun observeGuestState(roomId: String, guestId: String) {
        observeOpponentUseCase(roomId, guestId).onEach { playerState ->
            val current = uiState.value.opponentsProgress[guestId] ?: OpponentProgress()
            val updated = current.copy(
                solved     = playerState?.solved == true,
                failed     = playerState?.finishedAt != null && playerState.solved != true,
                guessCount = playerState?.currentRow ?: current.guessCount,
            )
            setState { copy(opponentsProgress = opponentsProgress + (guestId to updated)) }
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
            val progress = opponentsProgress[guestId] ?: OpponentProgress()
            copy(
                opponentsProgress = opponentsProgress + (guestId to progress.copy(name = name, avatarUrl = avatarUrl)),
                waitingPlayers    = waitingPlayers.filter { it.userId != guestId } +
                    WaitingPlayer(guestId, name, avatarUrl),
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
                } else if (s.roomStatus == "waiting") {
                    // Guest leaving the lobby: remove their name from the room
                    removeGuestFromRoomUseCase(s.roomId, s.myUserId)
                }
                // Guest leaving mid-game: just navigate back, room continues for others
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
            if (!isTargetWord && !s.isCustomWord) {
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

            val types = guess.mapIndexed { i, ch ->
                when {
                    ch == target[i] -> Types.CORRECT
                    ch in target    -> Types.PRESENT
                    else            -> Types.ABSENT
                }
            }

            val newBoard = s2.board.mapIndexed { r, row ->
                if (r == s2.currentRow) row.mapIndexed { c, tile ->
                    tile.copy(state = when (types.getOrElse(c) { Types.DEFAULT }) {
                        Types.CORRECT -> TileState.CORRECT
                        Types.PRESENT -> TileState.MISPLACED
                        Types.ABSENT  -> TileState.WRONG
                        else          -> TileState.EMPTY
                    })
                } else row
            }

            val newKeyboardStates = s2.keyboardStates.toMutableMap()
            guess.forEachIndexed { i, ch ->
                val current  = newKeyboardStates[ch]
                val incoming = types[i]
                val upgrade  = when (incoming) {
                    Types.CORRECT -> current != TileState.CORRECT
                    Types.PRESENT -> current != TileState.CORRECT && current != TileState.MISPLACED
                    Types.ABSENT  -> current == null
                    else          -> false
                }
                if (upgrade) newKeyboardStates[ch] = when (incoming) {
                    Types.CORRECT -> TileState.CORRECT
                    Types.PRESENT -> TileState.MISPLACED
                    Types.ABSENT  -> TileState.WRONG
                    else          -> TileState.EMPTY
                }
            }

            val solved   = types.all { it == Types.CORRECT }
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
                    // ── Custom word: show result locally, keep room alive ──────
                    setState { copy(isGameOver = true) }
                    sendEffect { MultiplayerGameEffect.ShowGameDialog(isWin = solved, targetWord = s2.targetWord) }
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
