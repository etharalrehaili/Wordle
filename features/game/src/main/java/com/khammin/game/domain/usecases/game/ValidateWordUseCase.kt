package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.GameRepository
import javax.inject.Inject

class ValidateWordUseCase @Inject constructor(
    private val repo: GameRepository
) {
    /**
     * Returns true if the word is valid.
     * Checks the local Strapi word list first; only calls the Claude endpoint
     * when the word is not found locally. Defaults to false on any error.
     */
    suspend operator fun invoke(word: String, language: String, localWordList: List<String>): Boolean {
        val normalised = word.trim().lowercase()
        if (localWordList.any { it.trim().lowercase() == normalised }) return true
        return try {
            repo.validateWord(word, language)
        } catch (_: Exception) {
            false
        }
    }
}
