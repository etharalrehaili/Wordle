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
import com.wordle.core.presentation.components.CustomSnackbarHost
import com.wordle.core.presentation.components.GameBoard
import com.wordle.core.presentation.components.GameKeyboard
import com.wordle.core.presentation.components.GuessRow
import com.wordle.core.presentation.components.SnackbarState
import com.wordle.core.presentation.components.bottomsheets.ChallengeResultBottomSheet
import com.wordle.core.presentation.components.bottomsheets.WordleInfoBottomSheet
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.core.presentation.components.enums.SnackbarType
import com.wordle.core.presentation.components.enums.TileState
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.theme.LocalWordleColors
import com.wordle.game.presentation.challenge.contract.ChallengeDialogState
import com.wordle.game.presentation.challenge.contract.ChallengeEffect
import com.wordle.game.presentation.challenge.contract.ChallengeIntent
import com.wordle.game.presentation.challenge.contract.ChallengeUiState
import com.wordle.game.presentation.game.contract.toTypes
import com.wordle.game.presentation.challenge.vm.ChallengeViewModel

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
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var snackbarState by remember { mutableStateOf<SnackbarState?>(null) }

    LaunchedEffect(currentLanguage, currentUid) {
        viewModel.onEvent(ChallengeIntent.LoadWords(currentLanguage.code))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ChallengeEffect.ShowGameDialog ->
                    dialogState = ChallengeDialogState.Result(effect.isWin, effect.targetWord)
                ChallengeEffect.NotInWordList -> snackbarState = SnackbarState(
                    message = "Not in word list",
                    type    = SnackbarType.WARNING
                )
                ChallengeEffect.InvalidWord -> { }
                ChallengeEffect.RowShake    -> { }
            }
        }
    }

    ChallengeContent(
        uiState         = uiState,
        currentLanguage = currentLanguage,
        dialogState     = dialogState,
        snackbarState   = snackbarState,
        onDismissSnackbar = { snackbarState = null },
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
    snackbarState: SnackbarState?,
    onDismissSnackbar: Action,
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

    val keyStates = uiState.keyboardStates.mapValues { (_, tileState) -> tileState.toTypes() }
    val hasNoChallenge = uiState.error != null && uiState.targetWord.isEmpty()

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

            if (hasNoChallenge) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = uiState.error ?: "No challenge for today",
                        color      = colors.body,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign  = TextAlign.Center,
                    )
                }
            } else {
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
        }

        snackbarState?.let {
            CustomSnackbarHost(
                state     = it,
                onDismiss = onDismissSnackbar,
            )
        }

        when (val dialog = dialogState) {
            ChallengeDialogState.Info -> {
                WordleInfoBottomSheet(
                    sheetState = infoSheetState,
                    onDismiss  = onDismissDialog,
                    wordLength = uiState.wordLength,
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
