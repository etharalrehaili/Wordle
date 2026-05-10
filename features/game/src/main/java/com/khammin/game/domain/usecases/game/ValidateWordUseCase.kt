package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.GameRepository
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

private const val VALIDATION_TIMEOUT_MS = 5_000L

class ValidateWordUseCase @Inject constructor(
    private val repo: GameRepository
) {
    suspend operator fun invoke(word: String, language: String, localWordList: List<String>): Boolean {
        val normalisedLower = word.trim().lowercase()
        if (localWordList.any { it.trim().lowercase() == normalisedLower }) return true

        // Network validation: cap at 5 s. On timeout or connectivity failure return false
        // so invalid words (like ابيس) are rejected rather than silently accepted.
        return try {
            withTimeout(VALIDATION_TIMEOUT_MS) {
                repo.validateWord(word, language)
            }
        } catch (_: Exception) {
            false
        }
    }
}