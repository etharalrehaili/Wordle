package com.wordle.game.domain.repository

import com.wordle.core.presentation.components.enums.TileState
import com.wordle.game.data.repository.SavedChallengeState
import com.wordle.game.presentation.game.contract.Tile
import kotlinx.coroutines.flow.Flow

interface ChallengeRepository {
    suspend fun getDailyChallenge(date: String, language: String): String?
    suspend fun loadTodayState(language: String): SavedChallengeState?
    suspend fun saveState(
        language: String,
        targetWord: String,
        board: List<List<Tile>>,
        keyboardStates: Map<Char, TileState>,
        currentRow: Int,
        currentCol: Int,
        isGameOver: Boolean,
        isWin: Boolean,
    )
    fun hasSolvedTodayChallenge(language: String): Flow<Boolean>
}