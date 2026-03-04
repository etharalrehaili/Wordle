package com.wordle.game.presentation

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.GameBoard
import com.wordle.core.presentation.components.GameKeyboard
import com.wordle.core.presentation.components.GuessRow
import com.wordle.core.presentation.components.bottomsheets.GameResultsDialog
import com.wordle.core.presentation.components.bottomsheets.WordleInfoBottomSheet
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.core.presentation.components.enums.Types
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.theme.LocalWordleColors
import com.wordle.game.presentation.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onClose: Action,
    currentLanguage: AppLanguage,
) {
    val colors = LocalWordleColors.current
    val uiState by viewModel.uiState.collectAsState()

    var showInfoSheet by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultIsWin by remember { mutableStateOf(false) }
    var resultWord by remember { mutableStateOf("") }

    val infoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val resultSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Load words when language changes
    LaunchedEffect(currentLanguage) {
        viewModel.onEvent(GameIntent.LoadWords(currentLanguage.code))
    }

    // Collect one-shot effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is GameEffect.ShowGameDialog -> {
                    resultIsWin = effect.isWin
                    resultWord = effect.targetWord
                    showResultDialog = true
                }
                GameEffect.InvalidWord -> { /* optional: show a toast/snackbar */ }
                GameEffect.RowShake    -> { /* optional: trigger shake animation */ }
            }
        }
    }

    // Map TileState → Types for the existing UI components
    fun TileState.toTypes(): Types = when (this) {
        TileState.CORRECT   -> Types.CORRECT
        TileState.MISPLACED -> Types.PRESENT
        TileState.WRONG     -> Types.ABSENT
        TileState.FILLED,
        TileState.EMPTY     -> Types.DEFAULT
    }

    // Convert board (List<List<Tile>>) → List<GuessRow> for GameBoard
    val guessRows = uiState.board.map { row ->
        GuessRow(
            letters = row.map { tile -> if (tile.state == TileState.EMPTY) null else tile.letter },
            types   = row.map { tile -> tile.state.toTypes() }
        )
    }

    // Convert keyboardStates (Map<Char, TileState>) → Map<Char, Types> for GameKeyboard
    val keyStates = uiState.keyboardStates.mapValues { (_, tileState) -> tileState.toTypes() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            GameTopBar(
                endIcon          = Icons.Filled.Close,
                startIcon        = Icons.Filled.Info,
                onEndIconClicked = { onClose() },
                onStartIconClicked = { showInfoSheet = true },
                modifier         = Modifier.fillMaxWidth()
            )

            // Board — takes remaining vertical space
            GameBoard(
                guesses    = guessRows,
                currentRow = uiState.currentRow,
                currentCol = uiState.currentCol,
                onKey      = { char -> viewModel.onEvent(GameIntent.EnterLetter(char)) },
                onEnter    = { viewModel.onEvent(GameIntent.SubmitGuess) },
                onBackspace = { viewModel.onEvent(GameIntent.DeleteLetter) },
                modifier   = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )

            // On-screen keyboard
            GameKeyboard(
                keyStates   = keyStates,
                onKey       = { char -> viewModel.onEvent(GameIntent.EnterLetter(char)) },
                onEnter     = { viewModel.onEvent(GameIntent.SubmitGuess) },
                onBackspace = { viewModel.onEvent(GameIntent.DeleteLetter) },
                language    = currentLanguage,
                modifier    = Modifier.fillMaxWidth()
            )
        }

        // Info bottom sheet
        if (showInfoSheet) {
            WordleInfoBottomSheet(
                sheetState = infoSheetState,
                onDismiss  = { showInfoSheet = false }
            )
        }

        // Win / Loss bottom sheet
        if (showResultDialog) {
            GameResultsDialog(
                title       = if (resultIsWin) "Brilliant! 🎉" else "Better Luck Next Time!",
                answer      = resultWord,
                accentColor = if (resultIsWin) colors.correct else colors.present,
                sheetState  = resultSheetState,
                onRestart   = {
                    showResultDialog = false
                    viewModel.onEvent(GameIntent.RestartGame)
                },
                onDismiss   = { showResultDialog = false }
            )
        }
    }
}