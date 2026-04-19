package com.khammin.game.presentation.game.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.GameBoard
import com.khammin.core.presentation.components.GameKeyboard
import com.khammin.core.presentation.components.GuessRow
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.core.presentation.components.enums.TileState
import com.khammin.core.presentation.components.multiplayer.GuestCard
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.components.toGuessRows
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.presentation.game.contract.MultiplayerGameIntent
import com.khammin.game.presentation.game.contract.OpponentProgress
import com.khammin.game.presentation.game.contract.WaitingPlayer
import com.khammin.game.presentation.game.contract.toTypes
import com.khammin.game.presentation.game.vm.MultiplayerGameViewModel
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.presentation.components.CustomSnackbarHost
import com.khammin.core.R as CoreRes
import com.khammin.game.R
import com.khammin.core.presentation.components.SnackbarState
import com.khammin.core.presentation.components.bottomsheets.GameMultiplayerResultBottomSheet
import com.khammin.core.presentation.components.bottomsheets.LeaveGameBottomSheet
import com.khammin.core.presentation.components.bottomsheets.CustomWordResultBottomSheet
import com.khammin.core.presentation.components.enums.SnackbarType
import com.khammin.core.presentation.components.enums.Types
import com.khammin.game.presentation.game.contract.MultiplayerGameEffect
import com.khammin.game.presentation.game.contract.MultiplayerGameUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerGameScreen(
    onClose: Action,
    currentLanguage: AppLanguage,
    roomId: String = "",
    isHost: Boolean = false,
    userId: String = "",
    isCustomWord: Boolean = false,
    viewModel: MultiplayerGameViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLeaveSheet by remember { mutableStateOf(false) }
    var showResultSheet by remember { mutableStateOf(false) }
    var resultIsWin by remember { mutableStateOf(false) }
    var resultWord by remember { mutableStateOf("") }
    var resultOpponentFailed by remember { mutableStateOf(false) }
    var resultOpponentLeft by remember { mutableStateOf(false) }
    var resultWinnerName by remember { mutableStateOf("") }
    val defaultMyName = stringResource(CoreRes.string.multiplayer_default_my_name)
    val defaultGuestName = stringResource(CoreRes.string.multiplayer_default_guest_name)
    var snackbarState by remember { mutableStateOf<SnackbarState?>(null) }
    var showHostLeftSheet by remember { mutableStateOf(false) }
    var showAllPlayersLeftSheet by remember { mutableStateOf(false) }
    var showNewWordSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val opponentGuesses = state.opponentState?.toGuessRows(state.wordLength)
        ?: List(MAX_GUESSES) { GuessRow() }

    BackHandler {
        when {
            state.isCustomWord -> showLeaveSheet = true
            state.opponentId.isNotEmpty() && !state.opponentLeft -> showLeaveSheet = true
            else -> onClose()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is MultiplayerGameEffect.ShowGameDialog -> {
                    resultIsWin = effect.isWin
                    resultWord = effect.targetWord
                    resultOpponentFailed = effect.opponentFailed
                    resultOpponentLeft = effect.opponentLeft
                    resultWinnerName = effect.winnerName
                    showResultSheet = true
                    android.util.Log.d("ResultSheet", "ShowGameDialog received: isWin=${effect.isWin}, targetWord=${effect.targetWord}, opponentFailed=${effect.opponentFailed}, opponentLeft=${effect.opponentLeft}")
                    android.util.Log.d("ResultSheet", "State at effect time: isHost=${state.isHost}, isCustomWord=${state.isCustomWord}")
                }

                is MultiplayerGameEffect.DismissResultDialog -> {
                    showResultSheet = false
                    resultWord = ""
                    resultOpponentFailed = false
                    resultOpponentLeft = false
                    resultWinnerName = ""
                }

                is MultiplayerGameEffect.NotInWordList -> snackbarState = SnackbarState(
                    context.getString(R.string.not_in_word_list),
                    SnackbarType.WARNING
                )

                is MultiplayerGameEffect.NavigateBack -> onClose()
                is MultiplayerGameEffect.HostLeftRoom -> showHostLeftSheet = true
                is MultiplayerGameEffect.AllPlayersLeft -> showAllPlayersLeftSheet = true
                else -> Unit
            }
        }
    }

    LaunchedEffect(state.isHostLeft) {
        if (state.isHostLeft) showHostLeftSheet = true
    }

    LaunchedEffect(roomId) {
        if (roomId.isNotEmpty()) {
            viewModel.onEvent(
                MultiplayerGameIntent.LoadGame(
                    roomId = roomId,
                    language         = "ar",
                    isHost = isHost,
                    myUserId = userId.takeIf { it.isNotEmpty() }
                        ?: FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    isCustomWord = isCustomWord,
                    defaultMyName = defaultMyName,
                    defaultGuestName = defaultGuestName,
                )
            )
        }
    }

    if (showLeaveSheet) {
        LeaveGameBottomSheet(
            onConfirm = {
                showLeaveSheet = false
                viewModel.onEvent(MultiplayerGameIntent.LeaveMatch)
            },
            onDismiss = { showLeaveSheet = false }
        )
    }

    if (showHostLeftSheet) {
        HostLeftBottomSheet(onGoHome = {
            showHostLeftSheet = false
            onClose()
        })
    }

    if (showAllPlayersLeftSheet) {
        AllPlayersLeftBottomSheet(onGoHome = {
            showAllPlayersLeftSheet = false
            viewModel.onEvent(MultiplayerGameIntent.LeaveMatch)
        })
    }

    if (showNewWordSheet) {
        var newWord by remember { mutableStateOf("") }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showNewWordSheet = false },
            sheetState       = sheetState,
            containerColor   = colors.background,
            dragHandle       = null,
            shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text       = stringResource(CoreRes.string.create_room_word_title),
                    color      = colors.title,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                OutlinedTextField(
                    value         = newWord,
                    onValueChange = { newWord = it.filter { c -> c.isLetter() } },
                    label         = { Text(stringResource(CoreRes.string.create_room_custom_hint), fontSize = 14.sp) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = colors.buttonTeal,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor     = colors.title,
                        unfocusedTextColor   = colors.title,
                        cursorColor          = colors.buttonTeal,
                        focusedLabelColor    = colors.buttonTeal,
                        unfocusedLabelColor  = colors.body.copy(alpha = 0.5f),
                    ),
                    shape         = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction      = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (newWord.length >= 3) {
                            showNewWordSheet = false
                            viewModel.onEvent(MultiplayerGameIntent.PlayAgainCustomWord(newWord))
                        }
                    }),
                )
                Button(
                    onClick  = {
                        showNewWordSheet = false
                        viewModel.onEvent(MultiplayerGameIntent.PlayAgainCustomWord(newWord))
                    },
                    enabled  = newWord.length >= 3,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = colors.buttonTeal,
                        disabledContainerColor = colors.buttonTeal.copy(alpha = 0.3f),
                    ),
                ) {
                    Text(
                        text       = stringResource(CoreRes.string.create_room_custom_action),
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = colors.background,
                    )
                }
            }
        }
    }

    if (showResultSheet) {
        android.util.Log.d("ResultSheet", "Showing sheet: isCustomWord=${state.isCustomWord}, isHost=${state.isHost}, resultIsWin=$resultIsWin, resultOpponentFailed=$resultOpponentFailed, resultOpponentLeft=$resultOpponentLeft")
        if (state.isCustomWord) {
            android.util.Log.d("ResultSheet", "→ CustomWordResultBottomSheet | isOwnWin=${!state.isHost && resultIsWin}, opponentGuessedCorrectly=${state.isHost && !resultOpponentFailed && !resultOpponentLeft}")
            CustomWordResultBottomSheet(
                opponentName = resultWinnerName.takeIf { it.isNotBlank() } ?: state.opponentName,
                targetWord = resultWord,
                opponentGuessedCorrectly = if (state.isHost) {
                    !resultOpponentFailed && !resultOpponentLeft
                } else {
                    false
                },
                opponentLeft = resultOpponentLeft,
                isOwnWin = if (state.isHost) false else resultIsWin,
                onPlayAgain = if (state.isHost) {
                    {
                        showResultSheet = false
                        resultWord = ""
                        showNewWordSheet = true
                    }
                } else null,
                playAgainVoteCount = state.playAgainVotes.size,
                totalGuests        = state.guestIds.size,
                onDismiss = { showResultSheet = false },
                onBackHome = {
                    showResultSheet = false
                    viewModel.onEvent(MultiplayerGameIntent.LeaveMatch)
                }
            )
        } else {
            android.util.Log.d("ResultSheet", "→ GameMultiplayerResultBottomSheet | isWin=$resultIsWin")
            GameMultiplayerResultBottomSheet(
                isWin = resultIsWin,
                targetWord = resultWord,
                myName = state.myName.takeIf { it.isNotBlank() }
                    ?: guestNameFromId(state.myUserId),
                opponentName = state.opponentName,
                opponentAvatarUrl = state.opponentAvatarUrl,
                opponentLeft = state.opponentLeft,
                opponentFailed = state.opponentFailed,
                onPlayAgain = if (state.opponentLeft) null else {
                    {
                        showResultSheet = false
                        resultWord = ""
                        viewModel.onEvent(MultiplayerGameIntent.RestartGame)
                    }
                },
                onDismiss = { showResultSheet = false },
                onBackHome = {
                    showResultSheet = false
                    viewModel.onEvent(MultiplayerGameIntent.LeaveMatch)
                }
            )
        }
    }

    MultiplayerGameContent(
        onClose = {
            when {
                state.isCustomWord -> showLeaveSheet = true
                state.opponentId.isNotEmpty() && !state.opponentLeft -> showLeaveSheet = true
                else -> onClose()
            }
        },
        roomId = roomId,
        isHost = isHost,
        state = state,
        opponentGuesses = opponentGuesses,
        onIntent = viewModel::onEvent,
        showResultButton = !showResultSheet && resultWord.isNotEmpty(),
        resultIsWin = resultIsWin,
        onShowResult = { showResultSheet = true },
        onPlayAgain = { showNewWordSheet = true },
    )

    snackbarState?.let {
        CustomSnackbarHost(
            state = it,
            onDismiss = { snackbarState = null },
        )
    }
}

@Composable
fun MultiplayerGameContent(
    onClose: Action,
    roomId: String = "",
    isHost: Boolean = false,
    state: MultiplayerGameUiState,
    opponentGuesses: List<GuessRow>,
    onIntent: (MultiplayerGameIntent) -> Unit,
    showResultButton: Boolean = false,
    resultIsWin: Boolean = false,
    onShowResult: () -> Unit = {},
    onPlayAgain: () -> Unit = {},
) {

    val layoutDirection = if (state.language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
    val keyboardLanguage = if (state.language == "ar") AppLanguage.ARABIC else AppLanguage.ENGLISH
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
            Column(modifier = Modifier.fillMaxSize()) {

                GameTopBar(
                    endIcon = Icons.Filled.Close,
                    startIcon = Icons.Filled.Info,
                    onEndIconClicked = onClose,
                    showBackground = false,
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                )

                // ── Lobby views (custom word, waiting state) ──────────────────────
                if (state.isCustomWord && !state.isHost && state.isGameOver) {
                    GuestGameOverLobby(
                        isWin              = state.isMyWin,
                        targetWord         = state.targetWord,
                        opponentsProgress  = state.opponentsProgress,
                        wordLength         = state.wordLength.takeIf { it > 0 } ?: 4,
                        roundNumber        = state.roundNumber,
                        hasVotedPlayAgain  = state.myUserId in state.playAgainVotes,
                        onVotePlayAgain    = { onIntent(MultiplayerGameIntent.VotePlayAgain) },
                        modifier           = Modifier.fillMaxWidth().weight(1f),
                    )
                } else if (state.isCustomWord && (state.roomStatus == "waiting" || (!state.isHost && state.isHostLeft))) {
                    if (state.isHost) {
                        CustomWordLobbyHost(
                            myName = state.myName,
                            avatarColor = state.avatarColor,
                            avatarEmoji = state.avatarEmoji,
                            waitingPlayers = state.waitingPlayers,
                            roomId = roomId,
                            onStart = { word -> onIntent(MultiplayerGameIntent.StartMatchWithWord(word)) },
                            onUpdateProfile = { name, color, emoji ->
                                onIntent(MultiplayerGameIntent.UpdateGuestProfile(name, color, emoji))
                            },
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    } else {
                        CustomWordLobbyGuest(
                            hostName = state.opponentName,
                            hostAvatarColor = state.opponentAvatarColor,
                            hostAvatarEmoji = state.opponentAvatarEmoji,
                            myName = state.myName,
                            avatarColor = state.avatarColor,
                            avatarEmoji = state.avatarEmoji,

                            otherPlayers = state.waitingPlayers.filter { it.userId != state.myUserId },
                            onUpdateProfile = { name, color, emoji ->
                                onIntent(MultiplayerGameIntent.UpdateGuestProfile(name, color, emoji))
                            },
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    }
                } else {
                    // ── In-game views ─────────────────────────────────────────────
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (state.isCustomWord) {
                            // Custom word mode: no single GuestCard; show result button if ready
                            if (showResultButton) {
                                ResultButton(isWin = resultIsWin, onClick = onShowResult)
                            } else {
                                Spacer(Modifier.size(0.dp))
                            }
                        } else {
                            if (state.opponentId.isNotEmpty()) {
                                GuestCard(
                                    name = state.opponentName,
                                    avatarUrl = state.opponentAvatarUrl,
                                    isLoading = state.isOpponentProfileLoading,
                                    guesses = opponentGuesses,
                                    wordLength = state.wordLength.takeIf { it > 0 } ?: 4,
                                )
                                if (showResultButton) {
                                    ResultButton(isWin = resultIsWin, onClick = onShowResult)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colors.surface)
                                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = "⏳", fontSize = 28.sp)
                                        Text(
                                            text = stringResource(CoreRes.string.multiplayer_waiting_opponent),
                                            color = colors.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                        )
                                        Text(
                                            text = stringResource(CoreRes.string.multiplayer_waiting_share),
                                            color = colors.body.copy(alpha = 0.45f),
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }

                            if (roomId.isNotEmpty() && isHost && state.opponentId.isEmpty()) {
                                RoomCodeCard(roomId = roomId)
                            }
                        }
                    }

                    if (state.isHost && state.isCustomWord) {
                        SpectatorView(
                            word               = state.targetWord,
                            wordLength         = state.wordLength.takeIf { it > 0 } ?: 4,
                            opponentsProgress  = state.opponentsProgress,
                            roundNumber        = state.roundNumber,
                            playAgainVoteCount = state.playAgainVotes.size,
                            totalGuests        = state.guestIds.size,
                            onPlayAgain        = onPlayAgain,
                            modifier           = Modifier.fillMaxWidth().weight(1f)
                        )
                    } else {
                        val keyboardEnabled = if (state.isCustomWord) {
                            state.roomStatus == "playing"
                        } else {
                            state.opponentId.isNotEmpty()
                        }

                        GameBoard(
                            guesses = state.board.map { row ->
                                GuessRow(
                                    letters = row.map { tile -> if (tile.state == TileState.EMPTY) null else tile.letter },
                                    types = row.map { tile -> tile.state.toTypes() }
                                )
                            },
                            currentRow = state.currentRow,
                            currentCol = state.currentCol,
                            wordLength = state.wordLength.takeIf { it > 0 } ?: 4,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )

                        GameKeyboard(
                            enabled = keyboardEnabled,
                            keyStates = state.keyboardStates.mapValues { (_, tileState) ->
                                when (tileState) {
                                    TileState.CORRECT -> Types.CORRECT
                                    TileState.MISPLACED -> Types.PRESENT
                                    TileState.WRONG -> Types.ABSENT
                                    else -> Types.DEFAULT
                                }
                            },
                            onKey = { if (keyboardEnabled) onIntent(MultiplayerGameIntent.EnterLetter(it)) },
                            onBackspace = { if (keyboardEnabled) onIntent(MultiplayerGameIntent.DeleteLetter) },
                            language = keyboardLanguage,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Lobby: host ───────────────────────────────────────────────────────────────

@Composable
private fun CustomWordLobbyHost(
    myName: String,
    avatarColor: Long?,
    avatarEmoji: String?,
    waitingPlayers: List<WaitingPlayer>,
    roomId: String,
    onStart: (word: String) -> Unit,
    onUpdateProfile: (name: String, color: Long?, emoji: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var customWord by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 12.dp, bottom = 32.dp),
    ) {
        // Room code
        item { RoomCodeCard(roomId = roomId) }

        // My profile card
        item {
            ProfileEditCard(
                myName = myName,
                avatarColor = avatarColor,
                avatarEmoji = avatarEmoji,
                onSave = onUpdateProfile,
            )
        }

        // Word input
        item {
            OutlinedTextField(
                value = customWord,
                onValueChange = { value ->
                    customWord = value.filter { it.isLetter() }.take(6).uppercase()
                },
                label = { Text("Your secret word (4–6 letters)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = colors.buttonTeal,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor    = colors.buttonTeal,
                    unfocusedLabelColor  = colors.body.copy(alpha = 0.5f),
                    focusedTextColor     = colors.title,
                    unfocusedTextColor   = colors.title,
                    cursorColor          = colors.buttonTeal,
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType   = KeyboardType.Text,
                    imeAction      = ImeAction.Done,
                ),
            )
        }

        // Player list header
        item {
            Text(
                text = "Players joined (${waitingPlayers.size + 1}/6)",
                color = colors.body.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Player rows inside a single card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
            ) {
                LobbyPlayerRow(
                    name = myName.ifBlank { "You" },
                    badge = "You",
                    badgeColor = colors.buttonPink,
                    avatarColor = avatarColor,
                    avatarEmoji = avatarEmoji,
                )
                if (waitingPlayers.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Waiting for players to join…",
                            color = colors.body.copy(alpha = 0.45f),
                            fontSize = 13.sp,
                        )
                    }
                } else {
                    waitingPlayers.forEach { player ->
                        LobbyPlayerRow(
                            name = player.name,
                            badge = "Opponent",
                            badgeColor = colors.body.copy(alpha = 0.6f),
                            avatarColor = player.avatarColor,
                            avatarEmoji = player.avatarEmoji,
                        )
                    }
                }
            }
        }

        // Start button
        item {
            Button(
                onClick = { onStart(customWord) },
                enabled = waitingPlayers.isNotEmpty() && customWord.length in 4..6,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonTeal,
                    disabledContainerColor = colors.buttonTeal.copy(alpha = 0.3f),
                )
            ) {
                Text(
                    text = "Start Game",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.background,
                )
            }
        }
    }
}

// ── Lobby: guest ──────────────────────────────────────────────────────────────

@Composable
private fun CustomWordLobbyGuest(
    hostName: String,
    hostAvatarColor: Long? = null,
    hostAvatarEmoji: String? = null,
    myName: String,
    avatarColor: Long?,
    avatarEmoji: String?,
    otherPlayers: List<WaitingPlayer>,
    onUpdateProfile: (name: String, color: Long?, emoji: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 32.dp),
    ) {
        item { Text(text = "⏳", fontSize = 48.sp) }

        item {
            Text(
                text = "Waiting for host to start…",
                color = colors.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        // My profile card
        item {
            ProfileEditCard(
                myName = myName,
                avatarColor = avatarColor,
                avatarEmoji = avatarEmoji,
                onSave = onUpdateProfile,
            )
        }

        // Player list header
        item {
            val totalPlayers = 1 + 1 + otherPlayers.size // host + me + others
            Text(
                text = "Players joined ($totalPlayers/6)",
                color = colors.body.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Player rows inside a single card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
            ) {
                // Host row
                LobbyPlayerRow(
                    name = hostName.ifBlank { "Host" },
                    badge = "Host",
                    badgeColor = colors.buttonTeal,
                    avatarColor = hostAvatarColor,
                    avatarEmoji = hostAvatarEmoji,
                )

                // My row
                LobbyPlayerRow(
                    name = myName.ifBlank { "You" },
                    badge = "You",
                    badgeColor = colors.buttonPink,
                    avatarColor = avatarColor,
                    avatarEmoji = avatarEmoji,
                )

                // Other guests
                otherPlayers.forEach { player ->
                    LobbyPlayerRow(
                        name = player.name,
                        badge = "Opponent",
                        badgeColor = colors.body.copy(alpha = 0.6f),
                        avatarColor = player.avatarColor,
                        avatarEmoji = player.avatarEmoji,
                    )
                }
            }
        }
    }
}

// ── Guest game-over lobby ─────────────────────────────────────────────────────

@Composable
private fun GuestGameOverLobby(
    isWin: Boolean,
    targetWord: String,
    opponentsProgress: Map<String, OpponentProgress>,
    wordLength: Int,
    roundNumber: Int = 1,
    hasVotedPlayAgain: Boolean,
    onVotePlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = if (isWin) colors.buttonTeal else colors.buttonPink

    LazyColumn(
        modifier            = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Result header ─────────────────────────────────────────────────────
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text       = "Round $roundNumber",
                    color      = colors.buttonTeal,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )
                Text(text = if (isWin) "🎉" else "🤔", fontSize = 48.sp)
                Text(
                    text       = if (isWin) stringResource(CoreRes.string.spectator_result_you_guessed)
                                 else       stringResource(CoreRes.string.result_lose_title),
                    color      = accentColor,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign  = TextAlign.Center,
                )
                if (targetWord.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text      = stringResource(CoreRes.string.result_the_word_was),
                        color     = colors.body.copy(alpha = 0.55f),
                        fontSize  = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        targetWord.forEach { letter ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accentColor.copy(alpha = 0.15f))
                                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text       = letter.toString(),
                                    color      = accentColor,
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Opponents progress ────────────────────────────────────────────────
        if (opponentsProgress.isNotEmpty()) {
            item {
                Text(
                    text      = "Other players",
                    color     = colors.body.copy(alpha = 0.5f),
                    fontSize  = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    modifier  = Modifier.fillMaxWidth(),
                )
            }
            val sorted = opponentsProgress.values.sortedWith(
                compareByDescending<OpponentProgress> { it.solved }
                    .thenBy { if (it.solved) it.guessCount else Int.MAX_VALUE }
                    .thenByDescending { it.failed }
            )
            items(sorted) { progress ->
                val points = if (progress.solved) when (progress.guessCount) {
                    1    -> 100
                    2    -> 80
                    3    -> 60
                    4    -> 40
                    5    -> 20
                    else -> 10
                } else 0
                val statusText = when {
                    progress.solved -> "Solved in ${progress.guessCount} ✓  ·  $points pts"
                    progress.failed -> "Failed ✗"
                    progress.guessCount == 0 -> "Waiting…"
                    else -> "${progress.guessCount}/${MAX_GUESSES}"
                }
                val statusColor = when {
                    progress.solved -> colors.buttonTeal
                    progress.failed -> colors.buttonPink
                    else            -> colors.body.copy(alpha = 0.55f)
                }
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    GuestCard(
                        name       = progress.name,
                        avatarUrl  = progress.avatarUrl,
                        guesses    = progress.guessRows,
                        wordLength = wordLength,
                    )
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text       = statusText,
                                color      = statusColor,
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        if (progress.totalPoints > 0) {
                            Text(
                                text       = "Total: ${progress.totalPoints} pts",
                                color      = colors.body.copy(alpha = 0.5f),
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }

        // ── Play again vote button ────────────────────────────────────────────
        item {
            Button(
                onClick  = onVotePlayAgain,
                modifier = Modifier
                    .wrapContentWidth()
                    .height(48.dp)
                    .padding(horizontal = 32.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (hasVotedPlayAgain) colors.buttonTeal
                    else colors.surface,
                ),
            ) {
                Text(
                    text       = if (hasVotedPlayAgain) "✓  Voted to play again" else "👋  Play Again?",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (hasVotedPlayAgain) colors.background else colors.body,
                )
            }
        }

        // ── Waiting indicator ─────────────────────────────────────────────────
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(text = "⏳", fontSize = 22.sp)
                Text(
                    text      = "Waiting for host to start the next round…",
                    color     = colors.body.copy(alpha = 0.45f),
                    fontSize  = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ── Shared composables ────────────────────────────────────────────────────────

@Composable
private fun LobbyPlayerRow(
    name: String,
    badge: String? = null,
    badgeColor: androidx.compose.ui.graphics.Color = colors.buttonTeal,
    avatarColor: Long? = null,
    avatarEmoji: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (avatarColor != null && avatarEmoji != null) {
                EmojiAvatar(color = avatarColor, emoji = avatarEmoji, size = 32)
            } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    color = badgeColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            } // end else (no emoji avatar)
            Text(
                text = name,
                color = colors.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badge,
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ResultButton(isWin: Boolean, onClick: () -> Unit) {
    val accent = if (isWin) colors.buttonTeal else colors.buttonPink
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(text = "🏆", fontSize = 22.sp)
            Text(
                text       = stringResource(CoreRes.string.multiplayer_result_title),
                color      = accent,
                fontSize   = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun RoomCodeCard(roomId: String) {
    val context = LocalContext.current
    val shortCode = roomId.take(6).uppercase()
    val clipLabel = stringResource(CoreRes.string.room_code_copied_label)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            stringResource(CoreRes.string.multiplayer_room_code),
            color      = colors.body.copy(alpha = 0.5f),
            fontSize   = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                            as android.content.ClipboardManager
                    clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText(clipLabel, shortCode)
                    )
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = shortCode,
                color      = colors.title,
                fontSize   = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )
        }

        Text(
            text     = stringResource(CoreRes.string.multiplayer_room_tap_copy),
            color    = colors.body.copy(alpha = 0.35f),
            fontSize = 9.sp,
        )
    }
}

@Composable
private fun SpectatorView(
    word: String,
    wordLength: Int,
    opponentsProgress: Map<String, OpponentProgress>,
    roundNumber: Int = 1,
    playAgainVoteCount: Int = 0,
    totalGuests: Int = 0,
    onPlayAgain: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val roundOver = opponentsProgress.isNotEmpty() &&
        (opponentsProgress.values.any { it.solved } || opponentsProgress.values.all { it.failed })
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Round label
        Text(
            text       = "Round $roundNumber",
            color      = colors.buttonTeal,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )

        // Word tiles so the host always sees what word they set
        if (word.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                word.forEach { letter ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .border(1.5.dp, colors.buttonTeal, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text       = letter.toString(),
                            color      = colors.title,
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
            }
        }

        if (playAgainVoteCount > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.buttonTeal.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = "👋  Votes for play again: $playAgainVoteCount / $totalGuests",
                    color      = colors.buttonTeal,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        if (opponentsProgress.isEmpty()) {
            Text(
                text      = stringResource(CoreRes.string.spectator_waiting),
                color     = colors.body.copy(alpha = 0.65f),
                fontSize  = 14.sp,
                textAlign = TextAlign.Center,
            )
        } else {
            val sortedProgress = opponentsProgress.values.sortedWith(
                compareByDescending<OpponentProgress> { it.solved }
                    .thenBy { if (it.solved) it.guessCount else Int.MAX_VALUE }
                    .thenByDescending { it.failed }
            )

            LazyColumn(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(sortedProgress) { progress ->
                    val points = if (progress.solved) when (progress.guessCount) {
                        1    -> 100
                        2    -> 80
                        3    -> 60
                        4    -> 40
                        5    -> 20
                        else -> 10
                    } else 0
                    val statusText = when {
                        progress.solved -> "Solved in ${progress.guessCount} ✓  ·  $points pts"
                        progress.failed -> "Failed ✗"
                        progress.guessCount == 0 -> "Waiting…"
                        else -> "${progress.guessCount}/${MAX_GUESSES}"
                    }
                    val statusColor = when {
                        progress.solved -> colors.buttonTeal
                        progress.failed -> colors.buttonPink
                        else            -> colors.body.copy(alpha = 0.55f)
                    }

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        GuestCard(
                            name       = progress.name,
                            avatarUrl  = progress.avatarUrl,
                            guesses    = progress.guessRows,
                            wordLength = wordLength,
                        )

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text       = statusText,
                                    color      = statusColor,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            if (progress.totalPoints > 0) {
                                Text(
                                    text       = "Total: ${progress.totalPoints} pts",
                                    color      = colors.body.copy(alpha = 0.5f),
                                    fontSize   = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick  = onPlayAgain,
            enabled  = roundOver,
            modifier = Modifier
                .wrapContentWidth()
                .height(48.dp)
                .padding(horizontal = 32.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = colors.buttonTeal,
                disabledContainerColor = colors.buttonTeal.copy(alpha = 0.3f),
            ),
        ) {
            Text(
                text       = stringResource(CoreRes.string.result_play_again),
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = colors.background,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HostLeftBottomSheet(onGoHome: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onGoHome,
        sheetState = sheetState,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "🚪", fontSize = 48.sp)
            Text(
                text = "Host left the room",
                color = colors.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "The host has left. The room is now closed.",
                color = colors.body.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.buttonTeal),
            ) {
                Text(
                    text = "Back to Home",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.background,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllPlayersLeftBottomSheet(onGoHome: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onGoHome,
        sheetState       = sheetState,
        containerColor   = colors.surface,
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "👥", fontSize = 48.sp)
            Text(
                text       = "All players left the room",
                color      = colors.title,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
            )
            Text(
                text      = "Everyone has left. The room is now empty.",
                color     = colors.body.copy(alpha = 0.6f),
                fontSize  = 14.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick  = onGoHome,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = colors.buttonPink),
            ) {
                Text(
                    text       = stringResource(CoreRes.string.multiplayer_result_back_home),
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = colors.background,
                )
            }
        }
    }
}

// ── Avatar colors & emojis ────────────────────────────────────────────────────

private val avatarColorOptions = listOf(
    0xFFE53935L, // Red
    0xFFFB8C00L, // Orange
    0xFFFDD835L, // Yellow
    0xFF43A047L, // Green
    0xFF00897BL, // Teal
    0xFF1E88E5L, // Blue
    0xFF8E24AAL, // Purple
    0xFFD81B60L, // Pink
    0xFF6D4C41L, // Brown
    0xFF757575L, // Gray
)

private val avatarEmojiOptions = listOf(
    "😎", "🐱", "🦊", "🐼", "🎮", "⭐", "🔥", "🌙", "🎯", "🦁",
    "🐸", "🤖", "👾", "🎲", "🌈", "🦋", "🐧", "🦄", "🎪", "🎭",
)

// ── Emoji avatar (no network) ─────────────────────────────────────────────────

@Composable
private fun EmojiAvatar(color: Long, emoji: String, size: Int = 44) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(color)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = (size * 0.52f).sp)
    }
}

// ── Profile card shown in lobby (edit for anonymous, read-only otherwise) ─────

@Composable
private fun ProfileEditCard(
    myName: String,
    avatarColor: Long?,
    avatarEmoji: String?,
    onSave: (name: String, color: Long?, emoji: String?) -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }
    var draftName by remember(myName) { mutableStateOf(myName) }
    // null = user selected the default letter-based avatar
    var draftColor by remember(avatarColor) { mutableStateOf(avatarColor) }
    var draftEmoji by remember(avatarEmoji) { mutableStateOf(avatarEmoji) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Header row ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val previewColor = if (isEditing) draftColor else avatarColor
                val previewEmoji = if (isEditing) draftEmoji else avatarEmoji
                if (previewColor != null && previewEmoji != null) {
                    EmojiAvatar(color = previewColor, emoji = previewEmoji, size = 44)
                } else {
                    // Default: letter circle, same style as LobbyPlayerRow
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.buttonPink.copy(alpha = 0.15f))
                            .border(1.dp, colors.buttonPink.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = myName.take(1).uppercase().ifBlank { "?" },
                            color = colors.buttonPink,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Column {
                    Text(
                        text = (if (isEditing) draftName else myName).ifBlank { "You" },
                        color = colors.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "You",
                        color = colors.body.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                    )
                }
            }
            if (!isEditing) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.buttonTeal.copy(alpha = 0.12f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { isEditing = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "Edit",
                        color = colors.buttonTeal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // ── Inline edit mode ─────────────────────────────────────────────────
        if (isEditing) {
            // Name field
            OutlinedTextField(
                value = draftName,
                onValueChange = { if (it.length <= 15) draftName = it },
                label = { Text("Display name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.buttonTeal,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.buttonTeal,
                    unfocusedLabelColor = colors.body.copy(alpha = 0.5f),
                    focusedTextColor = colors.title,
                    unfocusedTextColor = colors.title,
                    cursorColor = colors.buttonTeal,
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
            )

            // Color picker
            Text(
                text = "Avatar color",
                color = colors.body.copy(alpha = 0.55f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Default slot — letter circle
                item {
                    val selected = draftColor == null
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.buttonTeal.copy(alpha = 0.15f))
                            .border(
                                width = if (selected) 2.5.dp else 1.dp,
                                color = if (selected) colors.title else colors.buttonTeal.copy(alpha = 0.3f),
                                shape = CircleShape,
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { draftColor = null; draftEmoji = null },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = draftName.take(1).uppercase().ifBlank { "?" },
                            color = colors.buttonTeal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                items(avatarColorOptions) { colorLong ->
                    val selected = colorLong == draftColor
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(colorLong))
                            .border(
                                width = if (selected) 2.5.dp else 0.dp,
                                color = if (selected) colors.title else Color.Transparent,
                                shape = CircleShape,
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                draftColor = colorLong
                                if (draftEmoji == null) draftEmoji = avatarEmojiOptions.first()
                            },
                    )
                }
            }

            // Emoji picker — hidden when default (null) color is selected
            if (draftColor != null) {
                Text(
                    text = "Avatar emoji",
                    color = colors.body.copy(alpha = 0.55f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Default slot — first letter of name
                    item {
                        val selected = draftEmoji == null
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) colors.buttonTeal.copy(alpha = 0.18f)
                                    else Color.Transparent
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { draftEmoji = null },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = draftName.take(1).uppercase().ifBlank { "?" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) colors.buttonTeal else colors.body,
                            )
                        }
                    }
                    items(avatarEmojiOptions) { emoji ->
                        val selected = emoji == draftEmoji
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) colors.buttonTeal.copy(alpha = 0.18f)
                                    else Color.Transparent
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { draftEmoji = emoji },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }
            }

            // Save / Close buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        draftName = myName
                        draftColor = avatarColor
                        draftEmoji = avatarEmoji
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surface,
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                ) {
                    Text(
                        text = "Close",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.body,
                    )
                }
                Button(
                    onClick = {
                        onSave(draftName, draftColor, draftEmoji)
                        isEditing = false
                    },
                    enabled = draftName.isNotBlank(),
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.buttonTeal,
                        disabledContainerColor = colors.buttonTeal.copy(alpha = 0.3f),
                    ),
                ) {
                    Text(
                        text = "Save",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.background,
                    )
                }
            }
        }
    }
}

private fun guestNameFromId(id: String): String {
    val suffix = if (id.startsWith("guest_"))
        id.removePrefix("guest_").take(5)
    else
        id.take(5)
    return "Guest-$suffix"
}
