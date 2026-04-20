package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class UpdateGuestProfileUseCase @Inject constructor(
    private val repo: MultiplayerRepository
) {
    suspend operator fun invoke(roomId: String, userId: String, name: String, avatarColor: Long?, avatarEmoji: String?) =
        repo.updateGuestProfile(roomId, userId, name, avatarColor, avatarEmoji)
}
