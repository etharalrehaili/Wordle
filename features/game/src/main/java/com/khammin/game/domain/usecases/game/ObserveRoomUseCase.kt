package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class ObserveRoomUseCase @Inject constructor(
    private val repo: MultiplayerRepository
) {
    operator fun invoke(roomId: String) = repo.observeRoom(roomId)
}