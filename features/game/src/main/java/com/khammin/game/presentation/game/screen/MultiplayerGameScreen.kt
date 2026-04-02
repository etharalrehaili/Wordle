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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.R
import com.khammin.core.presentation.components.bottomsheets.GameMultiplayerResultBottomSheet
import com.khammin.core.presentation.components.bottomsheets.LeaveGameBottomSheet
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
    viewModel: MultiplayerGameViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLeaveSheet by remember { mutableStateOf(false) }
    var showResultSheet by remember { mutableStateOf(false) }
    var resultIsWin     by remember { mutableStateOf(false) }
    var resultWord      by remember { mutableStateOf("") }
    val defaultMyName    = stringResource(R.string.multiplayer_default_my_name)
    val defaultGuestName = stringResource(R.string.multiplayer_default_guest_name)

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
                    resultIsWin    = effect.isWin
                    resultWord     = effect.targetWord
                    showResultSheet = true
                }
                is MultiplayerGameEffect.DismissResultDialog -> {
                    showResultSheet = false
                }
                is MultiplayerGameEffect.NavigateBack -> onClose()
                else -> Unit
            }
        }
    }

    LaunchedEffect(roomId) {
        if (roomId.isNotEmpty()) {
            viewModel.onEvent(
                MultiplayerGameIntent.LoadGame(
                    roomId           = roomId,
                    language         = currentLanguage.code,
                    isHost           = isHost,
                    myUserId         = userId.takeIf { it.isNotEmpty() }
                        ?: FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    defaultMyName    = defaultMyName,
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
        GameMultiplayerResultBottomSheet(
            isWin             = resultIsWin,
            targetWord        = resultWord,
            myName            = state.myName,
            opponentName      = state.opponentName,
            opponentAvatarUrl = state.opponentAvatarUrl,
            opponentLeft      = state.opponentLeft,
            opponentFailed    = state.opponentFailed,
            onPlayAgain       = if (state.opponentLeft) null else {
                {
                    showResultSheet = false
                    viewModel.onEvent(MultiplayerGameIntent.RestartGame)
                }
            },
            // Swipe-down / tap-outside: just hide the sheet, stay in the game
            onDismiss  = { showResultSheet = false },
            // "Back Home" button: hide the sheet AND leave the match
            onBackHome = {
                showResultSheet = false
                viewModel.onEvent(MultiplayerGameIntent.LeaveMatch)
            }
        )
    }

    MultiplayerGameContent(
        currentLanguage  = currentLanguage,
        onClose          = {
            if (state.opponentId.isNotEmpty() && !state.opponentLeft) showLeaveSheet = true
            else onClose()
        },
        roomId           = roomId,
        isHost           = isHost,
        state            = state,
        opponentGuesses  = opponentGuesses,
        onIntent         = viewModel::onEvent,
        showResultButton = !showResultSheet && resultWord.isNotEmpty(),
        resultIsWin      = resultIsWin,
        onShowResult     = { showResultSheet = true },
    )
}

@Composable
fun MultiplayerGameContent(
    currentLanguage: AppLanguage,
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

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            GameTopBar(
                endIcon          = Icons.Filled.Close,
                startIcon        = Icons.Filled.Info,
                onEndIconClicked = onClose,
                modifier         = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (state.opponentId.isNotEmpty()) {
                    GuestCard(
                        name       = state.opponentName,
                        avatarUrl  = state.opponentAvatarUrl,
                        isLoading  = state.isOpponentProfileLoading,
                        guesses    = opponentGuesses,
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
                                text       = stringResource(R.string.multiplayer_waiting_opponent),
                                color      = colors.title,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign  = TextAlign.Center,
                            )
                            Text(
                                text      = stringResource(R.string.multiplayer_waiting_share),
                                color     = colors.body.copy(alpha = 0.45f),
                                fontSize  = 11.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                if (roomId.isNotEmpty() && isHost && state.opponentId.isEmpty()) {
                    RoomCodeCard(roomId = roomId)
                }
            }

            GameBoard(
                guesses    = state.board.map { row ->
                    GuessRow(
                        letters = row.map { tile -> if (tile.state == TileState.EMPTY) null else tile.letter },
                        types   = row.map { tile -> tile.state.toTypes() }
                    )
                },
                currentRow = state.currentRow,
                currentCol = state.currentCol,
                wordLength = state.wordLength.takeIf { it > 0 } ?: 4,
                modifier   = Modifier.fillMaxWidth().weight(1f)
            )

//            Spacer(Modifier.weight(1f))

            GameKeyboard(
                enabled   = state.opponentId.isNotEmpty(),
                keyStates = state.keyboardStates.mapValues { (_, tileState) ->
                    when (tileState) {
                        TileState.CORRECT   -> Types.CORRECT
                        TileState.MISPLACED -> Types.PRESENT
                        TileState.WRONG     -> Types.ABSENT
                        else                -> Types.DEFAULT
                    }
                },
                onKey       = { if (state.opponentId.isNotEmpty()) onIntent(MultiplayerGameIntent.EnterLetter(it)) },
                onBackspace = { if (state.opponentId.isNotEmpty()) onIntent(MultiplayerGameIntent.DeleteLetter) },
                language    = currentLanguage,
                modifier    = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )
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
            Text(text = if (isWin) "🏆" else "😔", fontSize = 22.sp)
            Text(
                text       = stringResource(R.string.multiplayer_result_title),
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
    val clipLabel = stringResource(R.string.room_code_copied_label)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            stringResource(R.string.multiplayer_room_code),
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
            text     = stringResource(R.string.multiplayer_room_tap_copy),
            color    = colors.body.copy(alpha = 0.35f),
            fontSize = 9.sp,
        )
    }
}