package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class UpdatePresenceStateUseCase @Inject constructor(
    private val repo: MultiplayerRepository
) {
    suspend operator fun invoke(roomId: String, userId: String, isForeground: Boolean) =
        repo.updatePresenceState(roomId, userId, isForeground)
}
