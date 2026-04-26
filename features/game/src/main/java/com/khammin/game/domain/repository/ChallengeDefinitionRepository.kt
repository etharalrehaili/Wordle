package com.khammin.game.domain.repository

import com.khammin.game.domain.model.RemoteChallengeDefinition
import kotlinx.coroutines.flow.Flow

interface ChallengeDefinitionRepository {
    /** Real-time stream of all active challenge definitions from Firestore. */
    fun observeDefinitions(): Flow<List<RemoteChallengeDefinition>>

    /** One-shot read — used inside EvaluateChallengesUseCase to avoid listener overhead. */
    suspend fun getDefinitions(): List<RemoteChallengeDefinition>
}
