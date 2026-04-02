package com.khammin.game.presentation.game.vm

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.domain.model.PlayerState
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.presentation.components.enums.TileState
import com.khammin.core.presentation.components.enums.Types
import com.khammin.core.util.Resource
import com.khammin.game.domain.usecases.game.FinishRoomUseCase
import com.khammin.game.domain.usecases.game.GetWordsUseCase
import com.khammin.game.domain.usecases.game.LeaveRoomUseCase
import com.khammin.game.domain.usecases.game.ObserveOpponentUseCase
import com.khammin.game.domain.usecases.game.ObserveRoomUseCase
import com.khammin.game.domain.usecases.game.RestartRoomUseCase
import com.khammin.game.domain.usecases.game.UpdatePlayerStateUseCase
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import com.khammin.game.presentation.game.contract.MultiplayerGameEffect
import com.khammin.game.presentation.game.contract.MultiplayerGameIntent
import com.khammin.game.presentation.game.contract.MultiplayerGameUiState
import com.khammin.game.presentation.game.contract.Tile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RESET_TAG = "MultiplayerReset"

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
    private val auth: FirebaseAuth
) : BaseMviViewModel<MultiplayerGameIntent, MultiplayerGameUiState, MultiplayerGameEffect>(
    initialState = MultiplayerGameUiState()
) {

    override fun onEvent(intent: MultiplayerGameIntent) {
        when (intent) {
            is MultiplayerGameIntent.LoadGame -> loadGame(
                intent.roomId, intent.language, intent.isHost, intent.myUserId,
                intent.defaultMyName, intent.defaultGuestName
            )
            is MultiplayerGameIntent.EnterLetter -> enterLetter(intent.letter)
            MultiplayerGameIntent.DeleteLetter   -> deleteLetter()
            MultiplayerGameIntent.SubmitGuess    -> submitGuess()
            MultiplayerGameIntent.LeaveMatch -> leaveMatch()
            MultiplayerGameIntent.RestartGame    -> restartGame()
        }
    }

    private var observingOpponentId: String = ""
    private var cachedWords: List<String> = emptyList()

    private fun loadGame(roomId: String, language: String, isHost: Boolean, myUserId: String, defaultMyName: String, defaultGuestName: String) {
        val myId = myUserId.takeIf { it.isNotEmpty() }
            ?: auth.currentUser?.uid
            ?: uiState.value.myUserId.takeIf { it.isNotEmpty() }
            ?: return

        setState { copy(roomId = roomId, myUserId = myId, isHost = isHost, language = language, defaultMyName = defaultMyName, defaultGuestName = defaultGuestName, myName = defaultMyName) }

        viewModelScope.launch {
            if (!myId.startsWith("guest_")) {
                val result = getProfileUseCase(myId)
                if (result is Resource.Success) {
                    val name = result.data?.name?.takeIf { it.isNotBlank() } ?: defaultMyName
                    setState { copy(myName = name) }
                }
            }
        }


        observeRoomUseCase(roomId).onEach { room ->
            if (room != null) {
                val opponentId = if (room.hostId == myId) room.guestId else room.hostId

                val previousWordLength = uiState.value.wordLength
                setState {
                    copy(
                        targetWord = room.word.uppercase(),
                        wordLength = room.wordLength,
                        opponentId = opponentId,
                        isHost     = room.hostId == myId,
                        board      = if (wordLength == room.wordLength) board
                        else List(board.size) { List(room.wordLength) { Tile() } }
                    )
                }

                // Populate (or refresh) the word cache whenever word length is first known or changes
                if (cachedWords.isEmpty() || room.wordLength != previousWordLength) {
                    viewModelScope.launch {
                        val result = getWordsUseCase(language, room.wordLength)
                        if (result is Resource.Success) {
                            cachedWords = result.data
                            Log.d(RESET_TAG, "[Room observer] Word cache populated — ${cachedWords.size} words | lang=$language | wordLength=${room.wordLength}")
                        }
                    }
                }

                // Show result when game first finishes
                if (room.status == "finished" && !uiState.value.isGameOver) {
                    val iWon           = room.winnerId == myId
                    val iLeft          = room.leftBy == myId
                    val opponentLeft   = room.leftBy.isNotEmpty() && room.leftBy != myId
                    val opponentFailed = room.failedBy.isNotEmpty() && room.failedBy != myId

                    setState { copy(isGameOver = true) }

                    when {
                        iLeft -> { }
                        opponentLeft -> {
                            setState { copy(opponentLeft = true) }
                            sendEffect { MultiplayerGameEffect.ShowGameDialog(
                                isWin = true, targetWord = room.word, opponentLeft = true
                            )}
                        }
                        opponentFailed -> {
                            setState { copy(opponentFailed = true) }
                            sendEffect { MultiplayerGameEffect.ShowGameDialog(
                                isWin = true, targetWord = room.word, opponentFailed = true
                            )}
                        }
                        else -> sendEffect { MultiplayerGameEffect.ShowGameDialog(
                            isWin = iWon, targetWord = room.word,
                        )}
                    }
                }

                // Opponent leaves while result sheet is already open
                if (room.status == "finished" && uiState.value.isGameOver) {
                    val opponentJustLeft = room.leftBy.isNotEmpty()
                            && room.leftBy != myId
                            && !uiState.value.opponentLeft

                    if (opponentJustLeft) {
                        setState { copy(opponentLeft = true) }
                    }
                }

                // When room resets to playing, reset local state for both players
                if (room.status == "playing" && uiState.value.isGameOver) {
                    val role = if (uiState.value.isHost) "HOST" else "GUEST"
                    val receivedAt = System.currentTimeMillis()
                    Log.d(RESET_TAG, "[$role] Firestore status='playing' received — resetting board now | word=${room.word} | ts=$receivedAt")
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
                    Log.d(RESET_TAG, "[$role] Board reset complete after Firestore update | elapsed=${System.currentTimeMillis() - receivedAt}ms")
                    sendEffect { MultiplayerGameEffect.DismissResultDialog }
                }

                if (room.status == "restarting" && uiState.value.isGameOver) {
                    val role = if (uiState.value.isHost) "HOST" else "GUEST"
                    Log.d(RESET_TAG, "[$role] Firestore status='restarting' received — dismissing result dialog | ts=${System.currentTimeMillis()}")
                    sendEffect { MultiplayerGameEffect.DismissResultDialog }
                }

                if (opponentId.isNotEmpty() && opponentId != observingOpponentId) {
                    observingOpponentId = opponentId
                    observeOpponent(roomId, opponentId)
                    fetchOpponentName(opponentId)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchOpponentName(opponentId: String) {
        val defaultGuestName = uiState.value.defaultGuestName

        if (opponentId.startsWith("guest_")) {
            setState { copy(opponentName = defaultGuestName, opponentAvatarUrl = null) }
            return
        }

        setState { copy(isOpponentProfileLoading = true) }

        viewModelScope.launch {
            val result = getProfileUseCase(opponentId)
            when (result) {
                is Resource.Success -> {
                    android.util.Log.d("Avatar", "avatarUrl = ${result.data?.avatarUrl}")
                    setState {
                        copy(
                            opponentName             = result.data?.name?.takeIf { it.isNotBlank() } ?: defaultGuestName,
                            opponentAvatarUrl        = result.data?.avatarUrl,
                            isOpponentProfileLoading = false
                        )
                    }
                }
                else -> setState {
                    copy(
                        opponentName             = defaultGuestName,
                        opponentAvatarUrl        = null,
                        isOpponentProfileLoading = false
                    )
                }
            }
        }
    }

    private fun observeOpponent(roomId: String, opponentId: String) {
        observeOpponentUseCase(roomId, opponentId).onEach { state ->
            setState { copy(opponentState = state) }
        }.launchIn(viewModelScope)
    }

    private fun leaveMatch() {
        val s = uiState.value
        if (s.roomId.isEmpty() || s.myUserId.isEmpty()) {
            sendEffect { MultiplayerGameEffect.NavigateBack }
            return
        }
        viewModelScope.launch {
            leaveRoomUseCase(s.roomId, s.myUserId)
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

        // Auto-submit when the row is complete
        if (newCol == s.wordLength && !s.isGameOver) {
            submitGuess()
        }
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

    private fun submitGuess() {
        val s = uiState.value
        if (s.currentCol < s.wordLength || s.isGameOver) return

        val guess = s.board[s.currentRow].filter { it.letter != ' ' }.map { it.letter }
        val target = s.targetWord.uppercase().toList()

        // Evaluate each letter
        val types = guess.mapIndexed { i, ch ->
            when {
                ch == target[i] -> Types.CORRECT
                ch in target    -> Types.PRESENT
                else            -> Types.ABSENT
            }
        }

        // Update board with evaluated types
        val newBoard = s.board.mapIndexed { r, row ->
            if (r == s.currentRow) row.mapIndexed { c, tile ->
                tile.copy(state = when (types[c]) {
                    Types.CORRECT -> TileState.CORRECT
                    Types.PRESENT -> TileState.MISPLACED
                    Types.ABSENT  -> TileState.WRONG
                    else          -> TileState.EMPTY
                })
            } else row
        }

        // Update keyboard state
        val newKeyboardStates = s.keyboardStates.toMutableMap()
        guess.forEachIndexed { i, ch ->
            val current = newKeyboardStates[ch]
            val incoming = types[i]
            val shouldUpgrade = when (incoming) {
                Types.CORRECT -> current != TileState.CORRECT
                Types.PRESENT -> current != TileState.CORRECT && current != TileState.MISPLACED
                Types.ABSENT  -> current == null
                else          -> false
            }
            if (shouldUpgrade) {
                newKeyboardStates[ch] = when (incoming) {
                    Types.CORRECT -> TileState.CORRECT
                    Types.PRESENT -> TileState.MISPLACED
                    Types.ABSENT  -> TileState.WRONG
                    else          -> TileState.EMPTY
                }
            }
        }

        val solved   = types.all { it == Types.CORRECT }
        val newRow   = s.currentRow + 1
        val gameOver = solved || newRow >= newBoard.size

        setState {
            copy(
                board          = newBoard,
                currentRow     = newRow,
                currentCol     = 0,
                keyboardStates = newKeyboardStates,
            )
        }

        // Push to Firestore
        viewModelScope.launch {
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
                roomId = s.roomId,
                userId = s.myUserId,
                state  = PlayerState(
                    guesses    = allGuesses,
                    types      = allTypes,
                    currentRow = newRow,
                    currentCol = 0,
                    solved     = solved,
                    finishedAt = if (gameOver) System.currentTimeMillis() else null
                )
            )

            if (solved) {
                finishRoomUseCase(s.roomId, s.myUserId)
            } else if (gameOver) {
                val winner = s.opponentId.takeIf { it.isNotEmpty() } ?: ""
                finishRoomUseCase(s.roomId, winner, failedBy = s.myUserId)
            }
        }
    }

    private fun restartGame() {
        val s = uiState.value
        val role = if (s.isHost) "HOST" else "GUEST"
        val tapAt = System.currentTimeMillis()

        Log.d(RESET_TAG, "[$role] Play Again tapped — roomId=${s.roomId} | myId=${s.myUserId} | ts=$tapAt")

        val localResetStart = System.currentTimeMillis()
        setState {
            copy(
                currentRow     = 0,
                currentCol     = 0,
                board          = List(board.size) { List(wordLength) { Tile() } },
                keyboardStates = emptyMap(),
                isGameOver     = false,
            )
        }
        Log.d(RESET_TAG, "[$role] Local board reset done — elapsed=${System.currentTimeMillis() - localResetStart}ms")

        viewModelScope.launch {
            Log.d(RESET_TAG, "[$role] Attempting claimRestart (Firestore write) — roomId=${s.roomId}")
            val claimStart = System.currentTimeMillis()
            val claimed = try {
                restartRoomUseCase.claimRestart(s.roomId)
                Log.d(RESET_TAG, "[$role] claimRestart SUCCESS — elapsed=${System.currentTimeMillis() - claimStart}ms | total from tap=${System.currentTimeMillis() - tapAt}ms")
                true
            } catch (e: Exception) {
                Log.w(RESET_TAG, "[$role] claimRestart FAILED — opponent already claimed restart | error=${e.message} | elapsed=${System.currentTimeMillis() - claimStart}ms")
                false
            }

            if (!claimed) {
                Log.d(RESET_TAG, "[$role] Not the claimer — waiting for Firestore status='playing' to reset board")
                return@launch
            }

            Log.d(RESET_TAG, "[$role] Clearing player states — myId=${s.myUserId} | opponentId=${s.opponentId}")
            val clearStart = System.currentTimeMillis()
            updatePlayerStateUseCase(s.roomId, s.myUserId, PlayerState())
            if (s.opponentId.isNotEmpty()) {
                updatePlayerStateUseCase(s.roomId, s.opponentId, PlayerState())
            }
            Log.d(RESET_TAG, "[$role] Player states cleared — elapsed=${System.currentTimeMillis() - clearStart}ms")

            val wordStart = System.currentTimeMillis()
            val words = cachedWords.takeIf { it.isNotEmpty() }?.also {
                Log.d(RESET_TAG, "[$role] Using cached word list (${it.size} words) — 0ms fetch")
            } ?: run {
                Log.d(RESET_TAG, "[$role] Cache empty — fetching word list from network | lang=${s.language} | wordLength=${s.wordLength}")
                val result = getWordsUseCase(s.language, s.wordLength)
                if (result is Resource.Success) result.data else null
            }

            if (words == null) {
                Log.e(RESET_TAG, "[$role] Failed to get word list — restart aborted")
                return@launch
            }

            val newWord = words.randomOrNull() ?: run {
                Log.e(RESET_TAG, "[$role] Word list is empty — cannot restart")
                return@launch
            }
            Log.d(RESET_TAG, "[$role] Word selected — elapsed=${System.currentTimeMillis() - wordStart}ms | word=${newWord.uppercase()}")

            val restartStart = System.currentTimeMillis()
            restartRoomUseCase(s.roomId, newWord.uppercase(), s.wordLength)
            Log.d(RESET_TAG, "[$role] restartRoom Firestore write done — elapsed=${System.currentTimeMillis() - restartStart}ms | TOTAL from tap=${System.currentTimeMillis() - tapAt}ms")
            Log.d(RESET_TAG, "[$role] Opponent will reset when their observer receives status='playing' from Firestore")

            // Refresh cache in background for the next restart
            viewModelScope.launch {
                val refreshResult = getWordsUseCase(s.language, s.wordLength)
                if (refreshResult is Resource.Success) {
                    cachedWords = refreshResult.data
                    Log.d(RESET_TAG, "[$role] Word cache refreshed — ${cachedWords.size} words")
                }
            }
        }
    }
}