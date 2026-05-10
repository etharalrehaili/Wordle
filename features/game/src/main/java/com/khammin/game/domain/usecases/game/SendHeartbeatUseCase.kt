package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class SendHeartbeatUseCase @Inject constructor(
    private val repo: MultiplayerRepository,
) {
    suspend operator fun invoke(roomId: String, userId: String) =
        repo.sendHeartbeat(roomId, userId)
}
