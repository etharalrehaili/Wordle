package com.wordle.game.domain.usecases

import com.wordle.game.data.repository.SavedChallengeState
import com.wordle.game.domain.repository.ChallengeRepository
import javax.inject.Inject

class LoadTodayChallengeUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    suspend operator fun invoke(): SavedChallengeState? {
        return repository.loadTodayState()
    }
}