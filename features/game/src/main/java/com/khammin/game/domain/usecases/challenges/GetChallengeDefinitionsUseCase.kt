package com.khammin.game.domain.usecases.challenges

import com.khammin.game.domain.model.RemoteChallengeDefinition
import com.khammin.game.domain.repository.ChallengeDefinitionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChallengeDefinitionsUseCase @Inject constructor(
    private val repository: ChallengeDefinitionRepository,
) {
    operator fun invoke(): Flow<List<RemoteChallengeDefinition>> =
        repository.observeDefinitions()
}
