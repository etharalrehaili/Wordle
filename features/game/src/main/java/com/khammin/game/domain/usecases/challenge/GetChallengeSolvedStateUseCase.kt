package com.khammin.game.domain.usecases.challenge

import com.khammin.game.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// returns a Flow<Boolean> that emits whether the user has already finished today's challenge.
// Used by the Home screen to decide whether to show the countdown timer.

class GetChallengeSolvedStateUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    operator fun invoke(language: String): Flow<Boolean> =
        repository.hasSolvedTodayChallenge(language)
}