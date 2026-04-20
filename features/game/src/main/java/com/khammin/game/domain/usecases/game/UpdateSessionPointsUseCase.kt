package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class UpdateSessionPointsUseCase @Inject constructor(
    private val repo: MultiplayerRepository
) {
    suspend operator fun invoke(roomId: String, sessionPoints: Map<String, Int>) =
        repo.updateSessionPoints(roomId, sessionPoints)
}
