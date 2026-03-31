package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class FinishRoomUseCase @Inject constructor(
    private val repo: MultiplayerRepository
) {
    suspend operator fun invoke(roomId: String, winnerId: String, failedBy: String = "") =
        repo.finishRoom(roomId, winnerId, failedBy)
}