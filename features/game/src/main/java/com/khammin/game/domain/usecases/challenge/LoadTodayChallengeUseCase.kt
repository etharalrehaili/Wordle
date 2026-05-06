package com.khammin.game.domain.usecases.challenge

import com.khammin.game.data.repository.SavedChallengeState
import com.khammin.game.domain.repository.ChallengeRepository
import javax.inject.Inject

class LoadTodayChallengeUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    suspend operator fun invoke(language: String): SavedChallengeState? =
        repository.loadTodayState(language)
}