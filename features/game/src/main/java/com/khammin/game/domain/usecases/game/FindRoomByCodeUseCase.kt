package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class FindRoomByCodeUseCase @Inject constructor(
    private val repo: MultiplayerRepository
) {
    suspend operator fun invoke(shortCode: String): String? =
        repo.findRoomByCode(shortCode)
}