package com.khammin.game.data.repository

import com.khammin.game.data.remote.datasource.challenge.ChallengeProgressDataSource
import com.khammin.game.domain.model.ChallengeSnapshot
import com.khammin.game.domain.repository.ChallengeProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChallengeProgressRepositoryImpl @Inject constructor(
    private val dataSource: ChallengeProgressDataSource,
) : ChallengeProgressRepository {

    override fun observeSnapshot(uid: String): Flow<ChallengeSnapshot> =
        dataSource.observeSnapshot(uid)

    override suspend fun getSnapshot(uid: String): ChallengeSnapshot =
        dataSource.getSnapshot(uid)

    override suspend fun initializeIfNeeded(uid: String) =
        dataSource.initializeIfNeeded(uid)

    override suspend fun saveSnapshot(uid: String, snapshot: ChallengeSnapshot) =
        dataSource.saveSnapshot(uid, snapshot)
}
