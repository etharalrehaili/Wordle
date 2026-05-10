package com.khammin.game.presentation.game.screen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.khammin.core.alias.Action
import com.khammin.core.util.AdManager
import com.khammin.core.presentation.components.CustomSnackbarHost
import com.khammin.core.presentation.components.GameBoard
import com.khammin.core.presentation.components.GameKeyboard
import com.khammin.core.presentation.components.GuessRow
import com.khammin.core.presentation.components.SnackbarState
import com.khammin.core.presentation.components.bottomsheets.GameResultsBottomSheet
import com.khammin.core.presentation.components.bottomsheets.WordleInfoBottomSheet
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.core.presentation.components.enums.SnackbarType
import com.khammin.core.domain.model.TileState
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.R
import com.khammin.core.R as CoreRes
import com.khammin.game.presentation.game.contract.GameDialogState
import com.khammin.game.presentation.game.contract.GameEffect
import com.khammin.game.presentation.game.contract.GameIntent
import com.khammin.game.presentation.game.contract.GameUiState
import com.khammin.game.presentation.game.contract.toTypes
import com.khammin.game.presentation.game.vm.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameViewModel: GameViewModel = hiltViewModel(),
    onClose: Action,
    wordLength: Int
) {

    val uiState by gameViewModel.uiState.collectAsState()
    var dialogState by remember { mutableStateOf<GameDialogState>(GameDialogState.None) }
    var snackbarState by remember { mutableStateOf<SnackbarState?>(null) }
    val context = LocalContext.current

    // Load words when the screen is first shown or when the word length changes
    LaunchedEffect(wordLength) {
        Log.d("GameDebug", "GameScreen LaunchedEffect — dispatching LoadWords(language=ar, wordLength=$wordLength)")
        gameViewModel.onEvent(GameIntent.LoadWords("ar", wordLength))
        AdManager.preload(context)
    }

    // Handle one-time UI effects like showing dialogs or snackbars
    LaunchedEffect(Unit) {
        gameViewModel.uiEffect.collect { effect ->
            when (effect) {
                is GameEffect.ShowGameDialog -> dialogState =
                    GameDialogState.Result(effect.isWin, effect.targetWord, meaning = effect.meaning)

                GameEffect.NotInWordList -> snackbarState = SnackbarState(
                    context.getString(R.string.not_in_word_list),
                    SnackbarType.WARNING
                )
            }
        }
    }

    // Force RTL layout for Arabic
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        GameContent(
            uiState           = uiState,
            dialogState       = dialogState,
            snackbarState     = snackbarState,
            onDismissSnackbar = { snackbarState = null },
            onInfoClick       = { dialogState = GameDialogState.Info },
            onDismissDialog   = { dialogState = GameDialogState.None },
            onRestart = {
                dialogState = GameDialogState.None
                gameViewModel.onEvent(GameIntent.RestartGame)
            },
            onClose  = onClose,
            onIntent = gameViewModel::onEvent,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameContent(
    uiState: GameUiState,
    dialogState: GameDialogState,
    snackbarState: SnackbarState?,
    onDismissSnackbar: Action,
    onClose: Action,
    onInfoClick: Action,
    onDismissDialog: Action,
    onRestart: Action,
    onIntent: (GameIntent) -> Unit,
) {

    val infoSheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val resultSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var timerSeconds by remember { mutableIntStateOf(0) }
    val timerText = String.format(java.util.Locale.US, "%02d:%02d", timerSeconds / 60, timerSeconds % 60)

    val guessRows = uiState.board.map { row ->
        GuessRow(
            letters = row.map { tile -> if (tile.state == TileState.EMPTY) null else tile.letter },
            types   = row.map { tile -> tile.state.toTypes() }
        )
    }

    val keyStates = uiState.keyboardStates.mapValues { (_, tileState) -> tileState.toTypes() }

    // Log whenever targetWord changes so we can confirm words are loaded
    LaunchedEffect(uiState.targetWord) {
        Log.d("GameDebug", "GameContent targetWord changed — targetWord='${uiState.targetWord}', isLoading=${uiState.isLoading}, isGameOver=${uiState.isGameOver}, wordLength=${uiState.wordLength}")
        timerSeconds = 0
    }

    // Increment timer every second while the game is active
    LaunchedEffect(uiState.isGameOver, uiState.targetWord) {
        Log.d("GameDebug", "Timer LaunchedEffect triggered — isGameOver=${uiState.isGameOver}, targetWord='${uiState.targetWord}'")
        if (!uiState.isGameOver && uiState.targetWord.isNotEmpty()) {
            Log.d("GameDebug", "Timer STARTED")
            while (true) {
                delay(1000L)
                timerSeconds++
            }
        } else {
            Log.d("GameDebug", "Timer NOT started — ${if (uiState.isGameOver) "game is over" else "targetWord is empty"}")
        }
    }

    Scaffold(
        modifier            = Modifier.fillMaxSize(),
        containerColor      = colors.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            GameTopBar(
                title              = timerText,
                endIcon            = Icons.Filled.Close,
                startIcon          = Icons.Filled.Info,
                onEndIconClicked   = onClose,
                onStartIconClicked = onInfoClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
            )
        },
        bottomBar = {
            GameKeyboard(
                keyStates   = keyStates,
                onKey       = { char -> onIntent(GameIntent.EnterLetter(char)) },
                onBackspace = { onIntent(GameIntent.DeleteLetter) },
                language    = AppLanguage.ARABIC,
                enabled     = uiState.targetWord.isNotEmpty() && !uiState.isGameOver,
                modifier    = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                GameBoard(
                    guesses    = guessRows,
                    currentRow = uiState.currentRow,
                    currentCol = uiState.currentCol,
                    wordLength = uiState.wordLength,
                    modifier   = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            snackbarState?.let {
                CustomSnackbarHost(
                    state     = it,
                    onDismiss = onDismissSnackbar,
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
                        meaning     = dialog.meaning,
                        accentColor = if (dialog.isWin) colors.correct else colors.present,
                        sheetState  = resultSheetState,
                        onRestart   = onRestart,
                        onClose     = {
                            onDismissDialog()
                            onClose()
                        },
                        onDismiss   = onDismissDialog,
                    )
                }

                GameDialogState.None -> Unit
            }
        }
    }
}
