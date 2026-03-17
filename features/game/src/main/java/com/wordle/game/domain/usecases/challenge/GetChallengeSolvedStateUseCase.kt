package com.wordle.game.domain.usecases.challenge

import com.wordle.game.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// returns a Flow<Boolean> that emits whether the user has already finished today's challenge.
// Used by the Home screen to decide whether to show the countdown timer.

class GetChallengeSolvedStateUseCase @Inject constructor(
    private val challengeRepository: ChallengeRepository
) {
    operator fun invoke(): Flow<Boolean> =
        challengeRepository.hasSolvedTodayChallenge()
}