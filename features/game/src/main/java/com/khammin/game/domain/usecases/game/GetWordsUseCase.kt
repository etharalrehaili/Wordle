package com.khammin.game.domain.usecases.game

import com.khammin.core.util.Resource
import com.khammin.game.domain.repository.GameRepository
import javax.inject.Inject

class GetWordsUseCase @Inject constructor(
    private val repo: GameRepository
) {
    suspend operator fun invoke(language: String, wordLength: Int): Resource<List<String>> {
        return try {
            val words = repo.getWords(language, wordLength)
            if (words.isEmpty()) Resource.Error("No words found")
            else Resource.Success(words)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}