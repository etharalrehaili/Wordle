package com.khammin.game.data.remote.datasource.challenge

import com.khammin.game.domain.model.ChallengeSnapshot
import kotlinx.coroutines.flow.Flow

interface ChallengeProgressDataSource {
    fun observeSnapshot(uid: String): Flow<ChallengeSnapshot>
    suspend fun getSnapshot(uid: String): ChallengeSnapshot
    suspend fun initializeIfNeeded(uid: String)
    suspend fun saveSnapshot(uid: String, snapshot: ChallengeSnapshot)
}
