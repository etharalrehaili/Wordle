package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class UpdatePlayerSessionPointsUseCase @Inject constructor(
    private val repo: MultiplayerRepository
) {
    suspend operator fun invoke(roomId: String, userId: String, pts: Int) =
        repo.updatePlayerSessionPoints(roomId, userId, pts)
}
