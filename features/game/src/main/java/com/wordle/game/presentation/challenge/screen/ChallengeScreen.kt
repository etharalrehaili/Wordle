package com.wordle.game.presentation.challenge.screen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.GameBoard
import com.wordle.core.presentation.components.GameKeyboard
import com.wordle.core.presentation.components.GuessRow
import com.wordle.core.presentation.components.bottomsheets.WordleInfoBottomSheet
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.theme.LocalWordleColors
import com.wordle.game.R
import com.wordle.game.presentation.challenge.contract.ChallengeDialogState
import com.wordle.game.presentation.challenge.contract.ChallengeEffect
import com.wordle.game.presentation.challenge.contract.ChallengeIntent
import com.wordle.game.presentation.challenge.contract.ChallengeUiState
import com.wordle.game.presentation.game.contract.TileState
import com.wordle.game.presentation.game.contract.toTypes
import com.wordle.game.presentation.challenge.vm.ChallengeViewModel
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    viewModel: ChallengeViewModel = hiltViewModel(),
    onClose: Action,
    currentLanguage: AppLanguage,
) {
    val uiState by viewModel.uiState.collectAsState()
    var dialogState by remember { mutableStateOf<ChallengeDialogState>(ChallengeDialogState.None) }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""  // ← add

    LaunchedEffect(currentLanguage, currentUid) {
        viewModel.onEvent(ChallengeIntent.LoadWords(currentLanguage.code))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ChallengeEffect.ShowGameDialog ->
                    dialogState = ChallengeDialogState.Result(effect.isWin, effect.targetWord)
                ChallengeEffect.InvalidWord -> { }
                ChallengeEffect.RowShake    -> { }
            }
        }
    }

    ChallengeContent(
        uiState         = uiState,
        currentLanguage = currentLanguage,
        dialogState     = dialogState,
        onClose         = onClose,
        onInfoClick     = { dialogState = ChallengeDialogState.Info },
        onDismissDialog = { dialogState = ChallengeDialogState.None },
        onIntent        = viewModel::onEvent,
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeContent(
    uiState: ChallengeUiState,
    currentLanguage: AppLanguage,
    dialogState: ChallengeDialogState,
    onClose: Action,
    onInfoClick: Action,
    onDismissDialog: Action,
    onIntent: (ChallengeIntent) -> Unit,
) {
    val colors           = LocalWordleColors.current
    val infoSheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val resultSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val guessRows = uiState.board.map { row ->
        GuessRow(
            letters = row.map { tile -> if (tile.state == TileState.EMPTY) null else tile.letter },
            types   = row.map { tile -> tile.state.toTypes() }
        )
    }
    Log.d("ChallengeContent", "wordLength=${uiState.wordLength}, board rows=${uiState.board.size}, board[0].size=${uiState.board.firstOrNull()?.size}, guessRows[0].letters.size=${guessRows.firstOrNull()?.letters?.size}")

    val keyStates = uiState.keyboardStates.mapValues { (_, tileState) -> tileState.toTypes() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            GameTopBar(
                endIcon            = Icons.Filled.Close,
                startIcon          = Icons.Filled.Info,
                onEndIconClicked   = onClose,
                onStartIconClicked = onInfoClick,
                modifier           = Modifier.fillMaxWidth()
            )

            GameBoard(
                guesses    = guessRows,
                currentRow = uiState.currentRow,
                currentCol = uiState.currentCol,
                wordLength = uiState.wordLength,
                modifier   = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )

            GameKeyboard(
                keyStates   = keyStates,
                onKey       = { char -> onIntent(ChallengeIntent.EnterLetter(char)) },
                onBackspace = { onIntent(ChallengeIntent.DeleteLetter) },
                language    = currentLanguage,
                modifier    = Modifier.fillMaxWidth()
            )
        }

        when (val dialog = dialogState) {
            ChallengeDialogState.Info -> {
                WordleInfoBottomSheet(
                    sheetState = infoSheetState,
                    onDismiss  = onDismissDialog
                )
            }
            is ChallengeDialogState.Result -> {
                ChallengeResultBottomSheet(
                    isWin      = dialog.isWin,
                    targetWord = dialog.word,
                    sheetState = resultSheetState,
                    onDismiss  = onDismissDialog
                )
            }
            ChallengeDialogState.None -> Unit
        }
    }
}

// ─── Daily Challenge Result Bottom Sheet ─────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChallengeResultBottomSheet(
    isWin: Boolean,
    targetWord: String,
    sheetState: SheetState,
    onDismiss: Action,
) {
    // Tick-tock: recalculate remaining time every second
    var countdown by remember { mutableStateOf(secondsUntilMidnight()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            countdown = secondsUntilMidnight()
        }
    }

    val colors = LocalWordleColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = colors.background,
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Win / Lose headline
            Text(
                text       = if (isWin) stringResource(R.string.result_win_title)
                else       stringResource(R.string.result_lose_title),
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = if (isWin) colors.correct else colors.present,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Reveal the answer on a loss
            if (!isWin) {
                Text(
                    text      = stringResource(R.string.result_answer_label, targetWord),
                    fontSize  = 16.sp,
                    color     = colors.body,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
            }

            // Countdown label
            Text(
                text      = stringResource(R.string.challenge_next_word_label),
                fontSize  = 14.sp,
                color     = colors.body,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            // HH:MM:SS countdown
            Text(
                text       = countdown.toHhMmSs(),
                fontSize   = 32.sp,
                fontWeight = FontWeight.Bold,
                color      = colors.body,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Time helpers ─────────────────────────────────────────────────────────────

/** Returns the number of seconds from now until the next midnight (00:00:00). */
@RequiresApi(Build.VERSION_CODES.O)
private fun secondsUntilMidnight(): Long {
    val now       = LocalDateTime.now()
    val midnight  = now.toLocalDate().plusDays(1).atStartOfDay()
    return Duration.between(now, midnight).seconds
}

/** Formats a raw second count as "HH:MM:SS". */
private fun Long.toHhMmSs(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    return "%02d:%02d:%02d".format(h, m, s)
}