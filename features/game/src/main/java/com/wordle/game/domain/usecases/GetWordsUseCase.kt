package com.wordle.game.domain.usecases

import com.wordle.game.domain.repository.GameRepository

// GetWordsUseCase — fetches words from API, filters by WORD_LENGTH, handles errors
import com.wordle.core.util.Resource
import javax.inject.Inject

class GetWordsUseCase @Inject constructor(
    private val repo: GameRepository
) {
    suspend operator fun invoke(language: String): Resource<List<String>> {
        return try {
            val words = repo.getWords(language)
            if (words.isEmpty()) Resource.Error("No words found for language: $language")
            else Resource.Success(words)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}