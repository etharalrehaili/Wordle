package com.wordle.game.data.remote.datasource.game

import com.wordle.game.data.remote.api.GameApiService
import com.wordle.game.data.remote.model.WordItem
import javax.inject.Inject

class GameRemoteDataSourceImpl @Inject constructor(
    private val api: GameApiService
) : GameRemoteDataSource {

    override suspend fun getWords(language: String, wordLength: Int): List<WordItem> {
        return api.getWords(language, wordLength).data
    }
}