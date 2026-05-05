package com.khammin.game.domain.usecases.challenges

import com.khammin.game.domain.repository.ChallengeProgressRepository
import javax.inject.Inject

class InitializeChallengeProgressUseCase @Inject constructor(
    private val repository: ChallengeProgressRepository,
) {
    /** Creates the Firestore progress document if it does not exist yet. Idempotent. */
    suspend operator fun invoke(uid: String) = repository.initializeIfNeeded(uid)
}
