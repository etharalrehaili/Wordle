package com.khammin.game.domain.usecases.challenges

import com.khammin.game.domain.repository.ChallengeProgressRepository
import javax.inject.Inject

class InitializeChallengeProgressUseCase @Inject constructor(
    private val repository: ChallengeProgressRepository,
) {
    suspend operator fun invoke(uid: String) = repository.initializeIfNeeded(uid)
}
