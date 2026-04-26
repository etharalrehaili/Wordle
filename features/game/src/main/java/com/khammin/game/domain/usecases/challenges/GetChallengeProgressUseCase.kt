package com.khammin.game.domain.usecases.challenges

import com.khammin.game.domain.model.ChallengeSnapshot
import com.khammin.game.domain.repository.ChallengeProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChallengeProgressUseCase @Inject constructor(
    private val repository: ChallengeProgressRepository,
) {
    operator fun invoke(uid: String): Flow<ChallengeSnapshot> =
        repository.observeSnapshot(uid)
}
