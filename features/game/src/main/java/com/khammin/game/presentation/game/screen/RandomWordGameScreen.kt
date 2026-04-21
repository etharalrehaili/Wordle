package com.khammin.game.presentation.game.screen

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.CustomSnackbarHost
import com.khammin.core.presentation.components.GameBoard
import com.khammin.core.presentation.components.GameKeyboard
import com.khammin.core.presentation.components.GuessRow
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.SnackbarState
import com.khammin.core.presentation.components.bottomsheets.GameMultiplayerResultBottomSheet
import com.khammin.core.presentation.components.bottomsheets.LeaveGameBottomSheet
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.core.presentation.components.enums.SnackbarType
import com.khammin.core.presentation.components.enums.TileState
import com.khammin.core.presentation.components.enums.Types
import com.khammin.core.presentation.components.multiplayer.GuestCard
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.components.toGuessRows
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.R
import com.khammin.game.presentation.game.components.AllPlayersLeftBottomSheet
import com.khammin.game.presentation.game.components.HostLeftBottomSheet
import com.khammin.game.presentation.game.components.RandomWordGameOverLobby
import com.khammin.game.presentation.game.components.RandomWordLobbyGuest
import com.khammin.game.presentation.game.components.RandomWordLobbyHost
import com.khammin.game.presentation.game.components.RejoinBottomSheet
import com.khammin.game.presentation.game.components.ResultButton
import com.khammin.game.presentation.game.components.RoomCodeCard
import com.khammin.game.presentation.game.contract.MultiplayerGameEffect
import com.khammin.game.presentation.game.contract.MultiplayerGameIntent
import com.khammin.game.presentation.game.contract.MultiplayerGameUiState
import com.khammin.game.presentation.game.contract.toTypes
import com.khammin.game.presentation.game.vm.MultiplayerGameViewModel
import com.khammin.core.R as CoreRes

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomWordGameScreen(
    onClose: Action,
    currentLanguage: AppLanguage,
    roomId: String = "",
    isHost: Boolean = false,
    userId: String = "",
    isLobbyMode: Boolean = false,
    viewModel: MultiplayerGameViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLeaveSheet by remember { mutableStateOf(false) }
    var showResultSheet by remember { mutableStateOf(false) }
    var showResultButton by remember { mutableStateOf(false) }
    var resultIsWin by remember { mutableStateOf(false) }
    var resultWord by remember { mutableStateOf("") }
    var resultOpponentFailed by remember { mutableStateOf(false) }
    var resultOpponentLeft by remember { mutableStateOf(false) }
    val defaultMyName = stringResource(CoreRes.string.multiplayer_default_my_name)
    val defaultGuestName = stringResource(CoreRes.string.multiplayer_default_guest_name)
    var snackbarState by remember { mutableStateOf<SnackbarState?>(null) }
    var showHostLeftSheet by remember { mutableStateOf(false) }
    var showAllPlayersLeftSheet by remember { mutableStateOf(false) }
    var showRejoinSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val opponentGuesses = state.opponentState?.toGuessRows(state.wordLength)
        ?: List(MAX_GUESSES) { GuessRow() }

    BackHandler {
        when {
            state.isLobbyMode -> showLeaveSheet = true
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
                    showResultSheet = true
                    showResultButton = true
                }
                is MultiplayerGameEffect.DismissResultDialog -> {
                    showResultSheet = false
                    resultWord = ""
                    resultOpponentFailed = false
                    resultOpponentLeft = false
                }
                is MultiplayerGameEffect.NotInWordList -> snackbarState = SnackbarState(
                    context.getString(R.string.not_in_word_list),
                    SnackbarType.WARNING
                )
                is MultiplayerGameEffect.NavigateBack -> onClose()
                is MultiplayerGameEffect.HostLeftRoom -> showHostLeftSheet = true
                is MultiplayerGameEffect.AllPlayersLeft -> showAllPlayersLeftSheet = true
                is MultiplayerGameEffect.ShowRejoinSheet -> showRejoinSheet = true
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
                    language = "ar",
                    isHost = isHost,
                    myUserId = userId.takeIf { it.isNotEmpty() }
                        ?: FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    isCustomWord = false,
                    isLobbyMode = isLobbyMode,
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

    if (showRejoinSheet) {
        RejoinBottomSheet(
            onRejoin = {
                showRejoinSheet = false
                viewModel.onEvent(MultiplayerGameIntent.RejoinRoom)
            },
            onGoHome = {
                showRejoinSheet = false
                viewModel.onEvent(MultiplayerGameIntent.LeaveMatch)
            },
        )
    }

    if (showResultSheet) {
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

    RandomWordGameContent(
        onClose = {
            when {
                state.isLobbyMode -> showLeaveSheet = true
                state.opponentId.isNotEmpty() && !state.opponentLeft -> showLeaveSheet = true
                else -> onClose()
            }
        },
        roomId = roomId,
        isHost = isHost,
        state = state,
        opponentGuesses = opponentGuesses,
        onIntent = viewModel::onEvent,
        showResultButton = showResultButton && !showResultSheet,
        resultIsWin = resultIsWin,
        onShowResult = { showResultSheet = true },
    )

    snackbarState?.let {
        CustomSnackbarHost(
            state = it,
            onDismiss = { snackbarState = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RandomWordGameContent(
    onClose: Action,
    roomId: String,
    isHost: Boolean,
    state: MultiplayerGameUiState,
    opponentGuesses: List<GuessRow>,
    onIntent: (MultiplayerGameIntent) -> Unit,
    showResultButton: Boolean,
    resultIsWin: Boolean,
    onShowResult: () -> Unit,
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

                if (state.isLobbyMode && state.isGameOver) {
                    RandomWordGameOverLobby(
                        isWin             = state.isMyWin,
                        winnerName        = state.lobbyWinnerName,
                        targetWord        = state.targetWord,
                        myName            = state.myName,
                        myAvatarColor     = state.avatarColor,
                        myAvatarEmoji     = state.avatarEmoji,
                        myAvatarUrl       = state.avatarUrl,
                        myUserId          = state.myUserId,
                        roundNumber       = state.roundNumber,
                        opponentsProgress = state.opponentsProgress,
                        sessionPoints     = state.sessionPoints,
                        isHost            = state.isHost,
                        onPlayAgain       = { onIntent(MultiplayerGameIntent.PlayAgainLobbyMode) },
                        onLeave           = { onIntent(MultiplayerGameIntent.LeaveMatch) },
                        modifier          = Modifier.fillMaxWidth().weight(1f),
                    )
                } else if (state.isLobbyMode && state.roomStatus == "waiting") {
                    if (state.isHost) {
                        RandomWordLobbyHost(
                            myName          = state.myName,
                            avatarColor     = state.avatarColor,
                            avatarEmoji     = state.avatarEmoji,
                            avatarUrl       = state.avatarUrl,
                            waitingPlayers  = state.waitingPlayers,
                            roomId          = roomId,
                            onStart         = { onIntent(MultiplayerGameIntent.StartMatch) },
                            onUpdateProfile = { name, color, emoji ->
                                onIntent(MultiplayerGameIntent.UpdateGuestProfile(name, color, emoji))
                            },
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    } else {
                        RandomWordLobbyGuest(
                            hostName        = state.opponentName,
                            hostAvatarColor = state.opponentAvatarColor,
                            hostAvatarEmoji = state.opponentAvatarEmoji,
                            hostAvatarUrl   = state.opponentAvatarUrl,
                            myName          = state.myName,
                            avatarColor     = state.avatarColor,
                            avatarEmoji     = state.avatarEmoji,
                            avatarUrl       = state.avatarUrl,
                            otherPlayers    = state.waitingPlayers.filter {
                                it.userId != state.myUserId && it.userId != state.opponentId
                            },
                            onUpdateProfile = { name, color, emoji ->
                                onIntent(MultiplayerGameIntent.UpdateGuestProfile(name, color, emoji))
                            },
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    }
                } else {
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (state.isLobbyMode) {
                            val opponents = state.opponentsProgress.values.toList()
                            if (opponents.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    items(opponents) { progress ->
                                        GuestCard(
                                            name        = progress.name,
                                            avatarUrl   = progress.avatarUrl,
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
                        } else {
                            if (state.opponentId.isNotEmpty()) {
                                GuestCard(
                                    name      = state.opponentName,
                                    avatarUrl = state.opponentAvatarUrl,
                                    isLoading = state.isOpponentProfileLoading,
                                    guesses   = opponentGuesses,
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
                                            text       = stringResource(CoreRes.string.multiplayer_waiting_opponent),
                                            color      = colors.title,
                                            fontSize   = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign  = TextAlign.Center,
                                        )
                                        Text(
                                            text      = stringResource(CoreRes.string.multiplayer_waiting_share),
                                            color     = colors.body.copy(alpha = 0.45f),
                                            fontSize  = 11.sp,
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

                    val keyboardEnabled = if (state.isLobbyMode) {
                        state.roomStatus == "playing"
                    } else {
                        state.opponentId.isNotEmpty()
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        GameBoard(
                            guesses = state.board.map { row ->
                                GuessRow(
                                    letters = row.map { tile -> if (tile.state == TileState.EMPTY) null else tile.letter },
                                    types   = row.map { tile -> tile.state.toTypes() }
                                )
                            },
                            currentRow = state.currentRow,
                            currentCol = state.currentCol,
                            wordLength = state.wordLength.takeIf { it > 0 } ?: 4,
                            modifier   = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )

                        GameKeyboard(
                            enabled   = keyboardEnabled,
                            keyStates = state.keyboardStates.mapValues { (_, tileState) ->
                                when (tileState) {
                                    TileState.CORRECT   -> Types.CORRECT
                                    TileState.MISPLACED -> Types.PRESENT
                                    TileState.WRONG     -> Types.ABSENT
                                    TileState.SIMILAR   -> Types.SIMILAR
                                    else                -> Types.DEFAULT
                                }
                            },
                            onKey       = { if (keyboardEnabled) onIntent(MultiplayerGameIntent.EnterLetter(it)) },
                            onBackspace = { if (keyboardEnabled) onIntent(MultiplayerGameIntent.DeleteLetter) },
                            language    = keyboardLanguage,
                            modifier    = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 32.dp)
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
