package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.GameProgressRepository
import javax.inject.Inject

class RecordWinUseCase @Inject constructor(
    private val repository: GameProgressRepository
) {
    suspend operator fun invoke(wordLength: Int) = repository.recordWin(wordLength)
}
