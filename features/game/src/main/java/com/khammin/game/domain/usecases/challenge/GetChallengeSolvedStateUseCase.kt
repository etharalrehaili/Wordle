package com.khammin.game.domain.usecases.challenge

import com.khammin.game.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChallengeSolvedStateUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    operator fun invoke(language: String): Flow<Boolean> =
        repository.hasSolvedTodayChallenge(language)
}