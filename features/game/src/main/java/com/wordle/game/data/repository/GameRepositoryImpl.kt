package com.wordle.game.data.repository

import com.wordle.game.data.remote.datasource.game.GameRemoteDataSource
import com.wordle.game.domain.repository.GameRepository
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor(
    private val remote: GameRemoteDataSource
) : GameRepository {

    override suspend fun getWords(language: String, wordLength: Int): List<String> {
        return remote.getWords(language, wordLength)
    }

}