package com.khammin.game.domain.usecases.game

import com.khammin.core.domain.model.GameRoom
import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class GetRoomUseCase @Inject constructor(
    private val repo: MultiplayerRepository
) {
    suspend operator fun invoke(roomId: String): GameRoom? =
        repo.getRoom(roomId)
}