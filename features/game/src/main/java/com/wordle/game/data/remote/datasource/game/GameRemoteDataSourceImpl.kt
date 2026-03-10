package com.wordle.game.data.remote.datasource.game

import com.wordle.game.data.remote.api.GameApiService
import javax.inject.Inject

class GameRemoteDataSourceImpl @Inject constructor(
    private val api: GameApiService
) : GameRemoteDataSource {

    override suspend fun getWords(language: String, wordLength: Int): List<String> {
        val response = api.getWords(language, wordLength)
        return response.data.map { it.text.uppercase() }
    }
}