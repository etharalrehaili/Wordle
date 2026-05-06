package com.khammin.game.presentation.game.screen

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.CustomSnackbarHost
import com.khammin.core.presentation.components.GameBoard
import com.khammin.core.presentation.components.GameKeyboard
import com.khammin.core.presentation.components.GuessRow
import com.khammin.core.presentation.components.SnackbarState
import com.khammin.core.presentation.components.bottomsheets.CustomWordResultBottomSheet
import com.khammin.core.presentation.components.bottomsheets.CustomWordResultScreen
import com.khammin.core.presentation.components.bottomsheets.LeaveGameBottomSheet
import com.khammin.core.presentation.components.bottomsheets.NoInternetBottomSheet
import com.khammin.core.presentation.components.bottomsheets.SessionLeaderboardEntry
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.core.presentation.components.enums.SnackbarType
import com.khammin.core.domain.model.RoomStatus
import com.khammin.core.domain.model.TileState
import com.khammin.game.domain.model.Tile
import com.khammin.core.presentation.components.enums.Types
import com.khammin.core.presentation.components.multiplayer.GuestCard
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.R
import com.khammin.game.presentation.game.components.AllPlayersLeftBottomSheet
import com.khammin.game.presentation.game.components.CustomWordLobbyGuest
import com.khammin.game.presentation.game.components.CustomWordLobbyHost
import com.khammin.game.presentation.game.components.GuestGameOverLobby
import com.khammin.game.presentation.game.components.HostLeftBottomSheet
import com.khammin.game.presentation.game.components.RejoinBottomSheet
import com.khammin.game.presentation.game.components.SelfDisconnectedBottomSheet
import com.khammin.game.presentation.game.components.ResultButton
import com.khammin.game.presentation.game.components.SpectatorView
import com.khammin.game.presentation.game.contract.MultiplayerGameEffect
import com.khammin.game.presentation.game.contract.MultiplayerGameIntent
import com.khammin.game.presentation.game.contract.MultiplayerGameUiState
import com.khammin.game.presentation.game.contract.toTypes
import com.khammin.game.presentation.game.vm.MultiplayerGameViewModel
import com.khammin.core.R as CoreRes

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWordGameScreen(
    onClose: Action,
    currentLanguage: AppLanguage,
    roomId: String = "",
    isHost: Boolean = false,
    userId: String = "",
    viewModel: MultiplayerGameViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLeaveSheet by remember { mutableStateOf(false) }
    var showResultSheet by remember { mutableStateOf(false) }
    var showResultButton by remember { mutableStateOf(false) }
    var resultIsWin by remember { mutableStateOf(false) }
    var resultTargetWord by remember { mutableStateOf("") }
    var resultOpponentLeft by remember { mutableStateOf(false) }
    var resultWinnerName by remember { mutableStateOf("") }
    val defaultMyName = stringResource(CoreRes.string.multiplayer_default_my_name)
    val defaultGuestName = stringResource(CoreRes.string.multiplayer_default_guest_name)
    var snackbarState by remember { mutableStateOf<SnackbarState?>(null) }
    var showHostLeftSheet by remember { mutableStateOf(false) }
    var showAllPlayersLeftSheet by remember { mutableStateOf(false) }
    var showRejoinSheet by remember { mutableStateOf(false) }
    var showSelfDisconnectedSheet by remember { mutableStateOf(false) }
    var showNewWordSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler { showLeaveSheet = true }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is MultiplayerGameEffect.ShowGameDialog -> {
                    resultIsWin = effect.isWin
                    resultTargetWord = effect.targetWord
                    resultOpponentLeft = effect.opponentLeft
                    resultWinnerName = effect.winnerName
                    showResultSheet = true
                    showResultButton = true
                }
                is MultiplayerGameEffect.DismissResultDialog -> {
                    showResultSheet = false
                    resultTargetWord = ""
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
                is MultiplayerGameEffect.ShowRejoinSheet -> showRejoinSheet = true
                is MultiplayerGameEffect.SelfDisconnected -> showSelfDisconnectedSheet = true
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
                    myUserId = userId,
                    isCustomWord = true,
                    isLobbyMode = false,
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

    if (showSelfDisconnectedSheet) {
        SelfDisconnectedBottomSheet(onGoHome = {
            showSelfDisconnectedSheet = false
            viewModel.onEvent(MultiplayerGameIntent.LeaveMatch)
        })
    }

    if (state.isNoInternet) {
        NoInternetBottomSheet(
            onRetry   = { viewModel.onEvent(MultiplayerGameIntent.RetryConnectivity) },
            onDismiss = { viewModel.onEvent(MultiplayerGameIntent.RetryConnectivity) },
        )
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
                    onValueChange = { if (it.length <= 6) newWord = it.filter { c -> c.isLetter() } },
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
                    shape           = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction      = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (newWord.length in 4..6) {
                            showNewWordSheet = false
                            showResultButton = false
                            viewModel.onEvent(MultiplayerGameIntent.PlayAgainCustomWord(newWord))
                        }
                    }),
                )
                Button(
                    onClick  = {
                        showNewWordSheet = false
                        showResultButton = false
                        viewModel.onEvent(MultiplayerGameIntent.PlayAgainCustomWord(newWord))
                    },
                    enabled  = newWord.length in 4..6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
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
        val leaderboard = state.opponentsProgress.entries.map { (userId, progress) ->
            SessionLeaderboardEntry(
                name          = progress.name,
                avatarColor   = progress.avatarColor,
                avatarEmoji   = progress.avatarEmoji,
                sessionPoints = state.sessionPoints[userId] ?: 0,
                isMe          = userId == state.myUserId,
            )
        }
        val word = resultTargetWord.ifBlank { state.targetWord }
        val winnerName = resultWinnerName.ifBlank { state.opponentName }

        if (state.isHost) {
            CustomWordResultScreen(
                opponentName             = winnerName,
                targetWord               = word,
                opponentGuessedCorrectly = resultIsWin,
                opponentLeft             = resultOpponentLeft,
                isOwnWin                 = false,
                onPlayAgain              = { showResultSheet = false; showNewWordSheet = true },
                playAgainVoteCount       = state.playAgainVotes.size,
                totalGuests              = state.guestIds.size,
                leaderboard              = leaderboard,
                onBackHome               = { showResultSheet = false; viewModel.onEvent(MultiplayerGameIntent.LeaveMatch) },
                onDismiss                = { showResultSheet = false },
            )
        } else {
            CustomWordResultBottomSheet(
                opponentName             = winnerName,
                targetWord               = word,
                opponentGuessedCorrectly = resultIsWin,
                opponentLeft             = resultOpponentLeft,
                isOwnWin                 = false,
                onPlayAgain              = null,
                playAgainVoteCount       = state.playAgainVotes.size,
                totalGuests              = state.guestIds.size,
                leaderboard              = leaderboard,
                onBackHome               = { showResultSheet = false; viewModel.onEvent(MultiplayerGameIntent.LeaveMatch) },
                onDismiss                = { showResultSheet = false },
            )
        }
    }

    CustomWordGameContent(
        onClose          = { showLeaveSheet = true },
        roomId           = roomId,
        state            = state,
        onIntent         = viewModel::onEvent,
        showResultButton = showResultButton && !showResultSheet,
        resultIsWin      = resultIsWin,
        onShowResult     = { showResultSheet = true },
        onPlayAgain      = { showNewWordSheet = true },
    )

    snackbarState?.let {
        CustomSnackbarHost(
            state    = it,
            onDismiss = { snackbarState = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomWordGameContent(
    onClose: Action,
    roomId: String,
    state: MultiplayerGameUiState,
    onIntent: (MultiplayerGameIntent) -> Unit,
    showResultButton: Boolean,
    resultIsWin: Boolean,
    onShowResult: () -> Unit,
    onPlayAgain: () -> Unit,
) {
    val layoutDirection = if (state.language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
    val keyboardLanguage = if (state.language == "ar") AppLanguage.ARABIC else AppLanguage.ENGLISH
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
            Column(modifier = Modifier.fillMaxSize()) {

                GameTopBar(
                    endIcon          = Icons.Filled.Close,
                    startIcon        = Icons.Filled.Info,
                    onEndIconClicked = onClose,
                    modifier         = Modifier.fillMaxWidth().statusBarsPadding()
                )

                if (!state.isHost && state.isGameOver) {
                    GuestGameOverLobby(
                        isWin             = state.isMyWin,
                        targetWord        = state.targetWord,
                        opponentsProgress = state.opponentsProgress,
                        wordLength        = state.wordLength.takeIf { it > 0 } ?: 4,
                        roundNumber       = state.roundNumber,
                        myName            = state.myName,
                        myAvatarColor     = state.avatarColor,
                        myAvatarEmoji     = state.avatarEmoji,
                        myUserId          = state.myUserId,
                        myGuessCount      = state.currentRow,
                        myTotalPoints     = state.sessionPoints[state.myUserId] ?: 0,
                        sessionPoints     = state.sessionPoints,
                        modifier          = Modifier.fillMaxWidth().weight(1f),
                    )
                } else if (state.roomStatus == RoomStatus.WAITING.value || (!state.isHost && state.isHostLeft)) {
                    if (state.isHost) {
                        CustomWordLobbyHost(
                            myName         = state.myName,
                            avatarColor    = state.avatarColor,
                            avatarEmoji    = state.avatarEmoji,
                            avatarUrl      = state.avatarUrl,
                            waitingPlayers = state.waitingPlayers,
                            roomId         = roomId,
                            onStart        = { word -> onIntent(MultiplayerGameIntent.StartMatchWithWord(word)) },
                            modifier       = Modifier.fillMaxWidth().weight(1f),
                        )
                    } else {
                        CustomWordLobbyGuest(
                            hostName        = state.opponentName,
                            hostAvatarColor = state.opponentAvatarColor,
                            hostAvatarEmoji = state.opponentAvatarEmoji,
                            hostAvatarUrl   = state.opponentAvatarUrl,
                            hostIsAfk        = state.opponentsProgress[state.opponentId]?.isAfk ?: state.isOpponentAfk,
                            hostAfkCountdown = state.opponentsProgress[state.opponentId]?.afkCountdown ?: state.opponentAfkCountdown,
                            myName          = state.myName,
                            avatarColor     = state.avatarColor,
                            avatarEmoji     = state.avatarEmoji,
                            avatarUrl       = state.avatarUrl,
                            otherPlayers    = state.waitingPlayers.filter { it.userId != state.myUserId },
                            isReady         = state.waitingPlayers.firstOrNull { it.userId == state.myUserId }?.isReady == true,
                            onSetReady      = { onIntent(MultiplayerGameIntent.SetReady) },
                            modifier        = Modifier.fillMaxWidth().weight(1f),
                        )
                    }
                } else {
                    Spacer(Modifier.height(8.dp))

                    if (!state.isHost) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val opponents = state.opponentsProgress.values.toList()
                            if (opponents.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    items(opponents) { progress ->
                                        GuestCard(
                                            name         = progress.name,
                                            avatarUrl    = progress.avatarUrl,
                                            avatarColor  = progress.avatarColor,
                                            avatarEmoji  = progress.avatarEmoji,
                                            isAfk        = progress.isAfk,
                                            afkCountdown = progress.afkCountdown,
                                            guesses      = progress.guessRows,
                                            wordLength   = state.wordLength.takeIf { it > 0 } ?: 4,
                                        )
                                    }
                                }
                            } else {
                                Spacer(Modifier.size(0.dp))
                            }
                            if (showResultButton) {
                                ResultButton(isWin = resultIsWin, onClick = onShowResult)
                            }
                        }
                    }

                    if (state.isHost) {
                        if (showResultButton) {
                            Row(
                                modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                ResultButton(isWin = true, onClick = onShowResult)
                            }
                        }
                        SpectatorView(
                            word              = state.targetWord,
                            wordLength        = state.wordLength.takeIf { it > 0 } ?: 4,
                            opponentsProgress = state.opponentsProgress,
                            roundNumber       = state.roundNumber,
                            onPlayAgain       = onPlayAgain,
                            modifier          = Modifier.fillMaxWidth().weight(1f),
                        )
                    } else {
                        val keyboardEnabled = state.roomStatus == RoomStatus.PLAYING.value

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
}
