package com.khammin.game.data.repository

import com.khammin.game.data.remote.datasource.challenge.ChallengeDefinitionDataSource
import com.khammin.game.domain.model.RemoteChallengeDefinition
import com.khammin.game.domain.repository.ChallengeDefinitionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChallengeDefinitionRepositoryImpl @Inject constructor(
    private val dataSource: ChallengeDefinitionDataSource,
) : ChallengeDefinitionRepository {

    override fun observeDefinitions(): Flow<List<RemoteChallengeDefinition>> =
        dataSource.observeDefinitions()

    override suspend fun getDefinitions(): List<RemoteChallengeDefinition> =
        dataSource.getDefinitions()
}
