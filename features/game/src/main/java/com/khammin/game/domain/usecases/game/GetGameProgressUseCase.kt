package com.khammin.game.domain.usecases.game

import com.khammin.core.domain.model.GameProgress
import com.khammin.game.domain.repository.GameProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGameProgressUseCase @Inject constructor(
    private val repository: GameProgressRepository
) {
    operator fun invoke(): Flow<GameProgress> = repository.getProgress()
}
