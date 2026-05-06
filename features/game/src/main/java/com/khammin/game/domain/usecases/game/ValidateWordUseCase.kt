package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.GameRepository
import javax.inject.Inject

class ValidateWordUseCase @Inject constructor(
    private val repo: GameRepository
) {
    suspend operator fun invoke(word: String, language: String, localWordList: List<String>): Boolean {
        val normalisedLower = word.trim().lowercase()

        if (localWordList.any { it.trim().lowercase() == normalisedLower }) return true

        return try {
            repo.validateWord(word, language)
        } catch (e: Exception) {
            false
        }
    }
}