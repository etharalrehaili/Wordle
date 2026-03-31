package com.khammin.game.domain.usecases.game

import com.khammin.core.domain.model.PlayerState
import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class UpdatePlayerStateUseCase @Inject constructor(
    private val repo: MultiplayerRepository
) {
    suspend operator fun invoke(roomId: String, userId: String, state: PlayerState) =
        repo.updatePlayerState(roomId, userId, state)
}