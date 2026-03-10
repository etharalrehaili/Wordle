package com.wordle.game.data.remote.datasource.game

interface GameRemoteDataSource {
    suspend fun getWords(language: String, wordLength: Int): List<String>
}