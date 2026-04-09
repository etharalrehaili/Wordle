package com.khammin.game.domain.repository

import com.khammin.core.domain.model.GameProgress
import kotlinx.coroutines.flow.Flow

interface GameProgressRepository {
    fun getProgress(): Flow<GameProgress>
    suspend fun recordWin(wordLength: Int)
}
