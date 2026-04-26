package com.khammin.game.data.remote.datasource.challenge

import com.khammin.game.domain.model.RemoteChallengeDefinition
import kotlinx.coroutines.flow.Flow

interface ChallengeDefinitionDataSource {
    fun observeDefinitions(): Flow<List<RemoteChallengeDefinition>>
    suspend fun getDefinitions(): List<RemoteChallengeDefinition>
}
