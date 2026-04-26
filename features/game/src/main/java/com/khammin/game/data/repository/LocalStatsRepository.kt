package com.khammin.game.data.repository

import com.khammin.game.data.local.LocalStatsDataStore
import com.khammin.game.domain.repository.StatsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStatsRepository @Inject constructor(
    private val dataStore: LocalStatsDataStore,
) : StatsRepository {

    override suspend fun recordGame(language: String, isWin: Boolean) {
        dataStore.recordGame(language, isWin)
    }
}
