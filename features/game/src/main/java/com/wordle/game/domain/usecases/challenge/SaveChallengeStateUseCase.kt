package com.wordle.game.domain.usecases.challenge

import com.wordle.game.domain.repository.ChallengeRepository
import com.wordle.game.presentation.game.contract.Tile
import com.wordle.game.presentation.game.contract.TileState
import javax.inject.Inject

class SaveChallengeStateUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    suspend operator fun invoke(
        targetWord: String,
        board: List<List<Tile>>,
        keyboardStates: Map<Char, TileState>,
        currentRow: Int,
        currentCol: Int,
        isGameOver: Boolean,
        isWin: Boolean,
    ) {
        repository.saveState(
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