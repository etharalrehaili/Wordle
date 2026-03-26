package com.khammin.game.domain.repository

import com.khammin.core.presentation.components.enums.TileState
import com.khammin.game.data.repository.SavedChallengeState
import com.khammin.game.presentation.game.contract.Tile
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