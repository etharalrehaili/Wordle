package com.wordle.game.domain.repository

import com.wordle.game.data.repository.SavedChallengeState
import com.wordle.game.presentation.game.contract.Tile
import com.wordle.game.presentation.game.contract.TileState

interface ChallengeRepository {
    suspend fun getDailyChallenge(date: String, language: String): String?
    suspend fun loadTodayState(): SavedChallengeState?
    suspend fun saveState(
        targetWord: String,
        board: List<List<Tile>>,
        keyboardStates: Map<Char, TileState>,
        currentRow: Int,
        currentCol: Int,
        isGameOver: Boolean,
        isWin: Boolean,
    )
}