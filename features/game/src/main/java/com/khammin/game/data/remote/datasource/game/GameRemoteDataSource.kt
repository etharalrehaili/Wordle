package com.khammin.game.data.remote.datasource.game

import com.khammin.game.data.remote.model.WordItem

interface GameRemoteDataSource {
    suspend fun getWords(language: String, wordLength: Int): List<WordItem>
    suspend fun validateWord(word: String, language: String): Boolean
}