package com.wordle.game.presentation.game.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.GameBoard
import com.wordle.core.presentation.components.GameKeyboard
import com.wordle.core.presentation.components.GuessRow
import com.wordle.core.presentation.components.bottomsheets.GameResultsBottomSheet
import com.wordle.core.presentation.components.bottomsheets.WordleInfoBottomSheet
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.core.presentation.components.enums.TileState
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.theme.LocalWordleColors
import com.wordle.core.R as CoreRes
import com.wordle.game.presentation.game.contract.GameDialogState
import com.wordle.game.presentation.game.contract.GameEffect
import com.wordle.game.presentation.game.contract.GameIntent
import com.wordle.game.presentation.game.contract.GameUiState
import com.wordle.game.presentation.game.contract.toTypes
import com.wordle.game.presentation.game.vm.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onClose: Action,
    currentLanguage: AppLanguage,
    wordLength: Int,
) {
    val uiState by viewModel.uiState.collectAsState()
    var dialogState by remember { mutableStateOf<GameDialogState>(GameDialogState.None) }

    LaunchedEffect(currentLanguage) {
        viewModel.onEvent(GameIntent.LoadWords(currentLanguage.code, wordLength))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is GameEffect.ShowGameDialog -> dialogState = GameDialogState.Result(effect.isWin, effect.targetWord)
                GameEffect.InvalidWord -> { }
                GameEffect.RowShake    -> { }
            }
        }
    }

    GameContent(
        uiState = uiState,
        currentLanguage = currentLanguage,
        dialogState = dialogState,
        onClose = onClose,
        onInfoClick = { dialogState = GameDialogState.Info },
        onDismissDialog = { dialogState = GameDialogState.None },
        onRestart = {
            dialogState = GameDialogState.None
            viewModel.onEvent(GameIntent.RestartGame)
        },
        onIntent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameContent(
    uiState: GameUiState,
    currentLanguage: AppLanguage,
    dialogState: GameDialogState,
    onClose: Action,
    onInfoClick: Action,
    onDismissDialog: Action,
    onRestart: Action,
    onIntent: (GameIntent) -> Unit,
) {
    val colors = LocalWordleColors.current
    val infoSheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val resultSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val guessRows = uiState.board.map { row ->
        GuessRow(
            letters = row.map { tile -> if (tile.state == TileState.EMPTY) null else tile.letter },
            types   = row.map { tile -> tile.state.toTypes() }
        )
    }

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
                onKey       = { char -> onIntent(GameIntent.EnterLetter(char)) },
                onBackspace = { onIntent(GameIntent.DeleteLetter) },
                language    = currentLanguage,
                modifier    = Modifier.fillMaxWidth()
            )
        }

        when (val dialog = dialogState) {
            GameDialogState.Info -> {
                WordleInfoBottomSheet(
                    sheetState = infoSheetState,
                    onDismiss  = onDismissDialog,
                    wordLength = uiState.wordLength,
                )
            }
            is GameDialogState.Result -> {
                GameResultsBottomSheet(
                    title       = if (dialog.isWin) stringResource(CoreRes.string.result_win_title)
                    else stringResource(CoreRes.string.result_lose_title),
                    answer      = dialog.word,
                    accentColor = if (dialog.isWin) colors.correct else colors.present,
                    sheetState  = resultSheetState,
                    onRestart   = onRestart,
                    onDismiss   = onDismissDialog,
                )
            }
            GameDialogState.None -> Unit
        }
    }
}