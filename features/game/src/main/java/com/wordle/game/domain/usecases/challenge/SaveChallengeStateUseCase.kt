package com.wordle.game.domain.usecases.challenge

import com.wordle.core.presentation.components.enums.TileState
import com.wordle.game.domain.repository.ChallengeRepository
import com.wordle.game.presentation.game.contract.Tile
import javax.inject.Inject

// persists the current board state to DataStore after every guess.
// Keeps progress safe if the user leaves the app.

class SaveChallengeStateUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    suspend operator fun invoke(
        language: String,
        targetWord: String,
        board: List<List<Tile>>,
        keyboardStates: Map<Char, TileState>,
        currentRow: Int,
        currentCol: Int,
        isGameOver: Boolean,
        isWin: Boolean,
    ) {
        repository.saveState(
            language = language,
            targetWord     = targetWord,
            board          = board,
            keyboardStates = keyboardStates,
            currentRow     = currentRow,
            currentCol     = currentCol,
            isGameOver     = isGameOver,
            isWin          = isWin,
        )
    }
}