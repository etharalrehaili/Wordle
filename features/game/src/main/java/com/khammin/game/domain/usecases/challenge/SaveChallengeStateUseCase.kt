package com.khammin.game.domain.usecases.challenge

import com.khammin.core.domain.model.TileState
import com.khammin.game.domain.model.Tile
import com.khammin.game.domain.repository.ChallengeRepository
import javax.inject.Inject


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