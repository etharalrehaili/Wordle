package com.khammin.game.domain.usecases.challenge

import com.khammin.game.data.repository.SavedChallengeState
import com.khammin.game.domain.repository.ChallengeRepository
import javax.inject.Inject


// loads the user's saved progress for today from DataStore.
// Returns the full board state, current row/col, keyboard state.
// Used to restore the game if the user left mid-game and came back.

class LoadTodayChallengeUseCase @Inject constructor(
    private val repository: ChallengeRepository
) {
    suspend operator fun invoke(language: String): SavedChallengeState? =
        repository.loadTodayState(language)
}