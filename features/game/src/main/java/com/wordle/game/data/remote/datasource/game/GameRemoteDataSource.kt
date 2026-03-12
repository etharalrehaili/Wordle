package com.wordle.game.data.remote.datasource.game

import com.wordle.game.data.remote.model.WordItem

interface GameRemoteDataSource {
    suspend fun getWords(language: String, wordLength: Int): List<WordItem>
}