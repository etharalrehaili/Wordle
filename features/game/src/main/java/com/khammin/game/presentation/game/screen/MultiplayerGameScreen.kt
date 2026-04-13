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
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val defaultMyName = stringResource(CoreRes.string.multiplayer_default_my_name)
    val defaultGuestName = stringResource(CoreRes.string.multiplayer_default_guest_name)
    var snackbarState by remember { mutableStateOf<SnackbarState?>(null) }
    val context = LocalContext.current

    val opponentGuesses = state.opponentState?.toGuessRows(state.wordLength)
        ?: List(MAX_GUESSES) { GuessRow() }

    BackHandler {
        if (state.opponentId.isNotEmpty() && !state.opponentLeft) {
            showLeaveSheet = true
        } else {
            onClose()
        }
    }

    // Collect NavigateBack effect
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is MultiplayerGameEffect.ShowGameDialog -> {
                    resultIsWin = effect.isWin
                    resultWord = effect.targetWord
                    resultOpponentFailed = effect.opponentFailed
                    resultOpponentLeft = effect.opponentLeft
                    showResultSheet = true
                    android.util.Log.d("ResultSheet", "ShowGameDialog received: isWin=${effect.isWin}, targetWord=${effect.targetWord}, opponentFailed=${effect.opponentFailed}, opponentLeft=${effect.opponentLeft}")
                    android.util.Log.d("ResultSheet", "State at effect time: isHost=${state.isHost}, isCustomWord=${state.isCustomWord}")
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
                else -> Unit
            }
        }
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

    if (showResultSheet) {
        android.util.Log.d("ResultSheet", "Showing sheet: isCustomWord=${state.isCustomWord}, isHost=${state.isHost}, resultIsWin=$resultIsWin, resultOpponentFailed=$resultOpponentFailed, resultOpponentLeft=$resultOpponentLeft")
        if (state.isCustomWord) {
            android.util.Log.d("ResultSheet", "→ CustomWordResultBottomSheet | isOwnWin=${!state.isHost && resultIsWin}, opponentGuessedCorrectly=${state.isHost && !resultOpponentFailed && !resultOpponentLeft}")
            CustomWordResultBottomSheet(
                opponentName = state.opponentName,
                targetWord = resultWord,
                opponentGuessedCorrectly = if (state.isHost) {
                    !resultOpponentFailed && !resultOpponentLeft
                } else {
                    false
                },
                opponentLeft = resultOpponentLeft,
                isOwnWin = if (state.isHost) false else resultIsWin,
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
                // Swipe-down / tap-outside: just hide the sheet, stay in the game
                onDismiss = { showResultSheet = false },
                // "Back Home" button: hide the sheet AND leave the match
                onBackHome = {
                    showResultSheet = false
                    viewModel.onEvent(MultiplayerGameIntent.LeaveMatch)
                }
            )
        }
    }

    MultiplayerGameContent(
        onClose = {
            if (state.opponentId.isNotEmpty() && !state.opponentLeft) showLeaveSheet = true
            else onClose()
        },
        roomId = roomId,
        isHost = isHost,
        state = state,
        opponentGuesses = opponentGuesses,
        onIntent = viewModel::onEvent,
        showResultButton = !showResultSheet && resultWord.isNotEmpty(),
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

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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

                if (state.isHost && state.isCustomWord) {
                    SpectatorView(
                        word = state.targetWord,
                        opponentName = state.opponentName,
                        opponentJoined = state.opponentId.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                } else {
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
                        enabled = state.opponentId.isNotEmpty(),
                        keyStates = state.keyboardStates.mapValues { (_, tileState) ->
                            when (tileState) {
                                TileState.CORRECT -> Types.CORRECT
                                TileState.MISPLACED -> Types.PRESENT
                                TileState.WRONG -> Types.ABSENT
                                else -> Types.DEFAULT
                            }
                        },
                        onKey = {
                            if (state.opponentId.isNotEmpty()) onIntent(
                                MultiplayerGameIntent.EnterLetter(
                                    it
                                )
                            )
                        },
                        onBackspace = {
                            if (state.opponentId.isNotEmpty()) onIntent(
                                MultiplayerGameIntent.DeleteLetter
                            )
                        },
                        language = keyboardLanguage,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                    )
                }
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
    opponentName: String,
    opponentJoined: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier         = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier            = Modifier.padding(horizontal = 32.dp),
        ) {
            Text(text = "👀", fontSize = 48.sp)
            Text(
                text       = stringResource(CoreRes.string.spectator_title),
                color      = colors.title,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
            )
            if (word.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    word.forEach { letter ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface)
                                .border(1.5.dp, colors.buttonTeal, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text       = letter.toString(),
                                color      = colors.title,
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }
                }
            }
            Text(
                text      = if (opponentJoined)
                    stringResource(CoreRes.string.spectator_watching, opponentName)
                else
                    stringResource(CoreRes.string.spectator_waiting),
                color     = colors.body.copy(alpha = 0.65f),
                fontSize  = 14.sp,
                textAlign = TextAlign.Center,
            )
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