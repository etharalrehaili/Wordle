package com.wordle.game.data.repository

import com.wordle.game.data.local.db.AppDatabase
import com.wordle.game.data.local.entity.WordEntity
import com.wordle.game.data.remote.datasource.game.GameRemoteDataSource
import com.wordle.game.domain.repository.GameRepository
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor(
    private val remote: GameRemoteDataSource,
    private val db: AppDatabase,
) : GameRepository {

    override suspend fun getWords(language: String, wordLength: Int): List<String> {
        // Check cache first
        val cached = db.wordDao().getWords(language, wordLength)
        if (cached.isNotEmpty()) {
                return cached.map { it.text.uppercase() }
        }

        // Cache empty — fetch from API
        val items = remote.getWords(language, wordLength)
        db.wordDao().insertWords(
            items.map {
                WordEntity(
                    id       = it.id,
                    text     = it.text,
                    language = language,
                    length   = wordLength,
                )
            }
        )
        return items.map { it.text.uppercase() }
    }
}