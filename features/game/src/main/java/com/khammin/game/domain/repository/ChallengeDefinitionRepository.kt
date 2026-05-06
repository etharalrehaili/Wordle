package com.khammin.game.domain.repository

import com.khammin.game.domain.model.RemoteChallengeDefinition
import kotlinx.coroutines.flow.Flow

interface ChallengeDefinitionRepository {
    fun observeDefinitions(): Flow<List<RemoteChallengeDefinition>>

    suspend fun getDefinitions(): List<RemoteChallengeDefinition>
}
