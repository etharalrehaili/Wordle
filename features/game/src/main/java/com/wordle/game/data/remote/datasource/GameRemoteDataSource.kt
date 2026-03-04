package com.wordle.game.data.remote.datasource

interface GameRemoteDataSource {
    suspend fun getWords(language: String): List<String>
}