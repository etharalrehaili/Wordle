package com.khammin.game.domain.repository

import com.khammin.game.data.remote.model.WordData

interface GameRepository {
    suspend fun getWords(language: String, wordLength: Int): List<WordData>
    suspend fun validateWord(word: String, language: String): Boolean
}
