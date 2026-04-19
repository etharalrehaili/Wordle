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
import com.khammin.game.presentation.game.contract.toTypes
import com.khammin.game.presentation.game.vm.MultiplayerGameViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.khammin.core.presentation.components.bottomsheets.SessionLeaderboardEntry
import com.khammin.core.presentation.components.enums.SnackbarType
import com.khammin.core.presentation.components.enums.Types
import com.khammin.game.presentation.game.components.AllPlayersLeftBottomSheet
import com.khammin.game.presentation.game.components.CustomWordLobbyGuest
import com.khammin.game.presentation.game.components.CustomWordLobbyHost
import com.khammin.game.presentation.game.components.GuestGameOverLobby
import com.khammin.game.presentation.game.components.HostLeftBottomSheet
import com.khammin.game.presentation.game.components.ResultButton
import com.khammin.game.presentation.game.components.RoomCodeCard
import com.khammin.game.presentation.game.components.SpectatorView
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
    var resultTotalPoints by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
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
                    resultTotalPoints = effect.totalPoints
                    showResultSheet = true
                }

                is MultiplayerGameEffect.DismissResultDialog -> {
                    showResultSheet = false
                    resultWord = ""
                    resultOpponentFailed = false
                    resultOpponentLeft = false
                    resultWinnerName = ""
                    resultTotalPoints = emptyMap()
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
        if (state.isCustomWord) {
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
                leaderboard = run {
                    val pts = resultTotalPoints.ifEmpty { state.sessionPoints }
                    state.opponentsProgress.entries.map { (guestId, p) ->
                        SessionLeaderboardEntry(
                            name          = p.name,
                            avatarColor   = p.avatarColor,
                            avatarEmoji   = p.avatarEmoji,
                            sessionPoints = pts[guestId] ?: 0,
                        )
                    }
                },
                onDismiss = { showResultSheet = false },
                onBackHome = {
                    showResultSheet = false
                    viewModel.onEvent(MultiplayerGameIntent.LeaveMatch)
                }
            )
        } else {
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
        resultWord = resultWord,
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
    resultWord: String = "",
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
                        isWin             = state.isMyWin,
                        targetWord        = state.targetWord,
                        opponentsProgress = state.opponentsProgress,
                        wordLength        = state.wordLength.takeIf { it > 0 } ?: 4,
                        roundNumber       = state.roundNumber,
                        myName            = state.myName,
                        myAvatarColor     = state.avatarColor,
                        myAvatarEmoji     = state.avatarEmoji,
                        myGuessCount      = state.currentRow,
                        myTotalPoints     = state.sessionPoints[state.myUserId] ?: 0,
                        sessionPoints     = state.sessionPoints,
                        hasVotedPlayAgain = state.myUserId in state.playAgainVotes,
                        onVotePlayAgain   = { onIntent(MultiplayerGameIntent.VotePlayAgain) },
                        modifier          = Modifier.fillMaxWidth().weight(1f),
                    )
                } else if (state.isCustomWord && (state.roomStatus == "waiting" || (!state.isHost && state.isHostLeft))) {
                    if (state.isHost) {
                        CustomWordLobbyHost(
                            myName          = state.myName,
                            avatarColor     = state.avatarColor,
                            avatarEmoji     = state.avatarEmoji,
                            waitingPlayers  = state.waitingPlayers,
                            roomId          = roomId,
                            onStart         = { word -> onIntent(MultiplayerGameIntent.StartMatchWithWord(word)) },
                            onUpdateProfile = { name, color, emoji ->
                                onIntent(MultiplayerGameIntent.UpdateGuestProfile(name, color, emoji))
                            },
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    } else {
                        CustomWordLobbyGuest(
                            hostName        = state.opponentName,
                            hostAvatarColor = state.opponentAvatarColor,
                            hostAvatarEmoji = state.opponentAvatarEmoji,
                            myName          = state.myName,
                            avatarColor     = state.avatarColor,
                            avatarEmoji     = state.avatarEmoji,
                            otherPlayers    = state.waitingPlayers.filter { it.userId != state.myUserId },
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
                        if (state.isCustomWord && !state.isHost) {
                            // Custom word guest: show all opponents' mini boards
                            val opponents = state.opponentsProgress.values.toList()
                            if (opponents.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    items(opponents) { progress ->
                                        GuestCard(
                                            name        = progress.name,
                                            avatarColor = progress.avatarColor,
                                            avatarEmoji = progress.avatarEmoji,
                                            guesses     = progress.guessRows,
                                            wordLength  = state.wordLength.takeIf { it > 0 } ?: 4,
                                        )
                                    }
                                }
                            } else {
                                Spacer(Modifier.size(0.dp))
                            }
                            if (showResultButton) {
                                ResultButton(isWin = resultIsWin, onClick = onShowResult)
                            }
                        } else if (!state.isCustomWord) {
                            // 1v1 mode only
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
                                if (roomId.isNotEmpty() && isHost) {
                                    RoomCodeCard(roomId = roomId)
                                }
                            }
                        }
                    }

                    if (state.isHost && state.isCustomWord) {
                        if (resultWord.isNotEmpty()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.End) {
                                ResultButton(isWin = true, onClick = onShowResult)
                            }
                        }
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

private fun guestNameFromId(id: String): String {
    val suffix = if (id.startsWith("guest_"))
        id.removePrefix("guest_").take(5)
    else
        id.take(5)
    return "Guest-$suffix"
}
