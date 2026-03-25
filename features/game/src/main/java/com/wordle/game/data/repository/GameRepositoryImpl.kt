package com.wordle.game.data.repository

import com.wordle.core.util.normalizeForWordle
import com.wordle.game.data.local.db.AppDatabase
import com.wordle.game.data.local.entity.WordEntity
import com.wordle.game.data.remote.datasource.game.GameRemoteDataSource
import com.wordle.game.data.remote.model.resolvedText
import com.wordle.game.domain.repository.GameRepository
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor(
    private val remote: GameRemoteDataSource,
    private val db: AppDatabase,
) : GameRepository {

    override suspend fun getWords(language: String, wordLength: Int): List<String> {
        return try {
            val items = remote.getWords(language, wordLength)
            if (items.isNotEmpty()) {
                val entities = items.mapNotNull { item ->
                    val t = item.resolvedText().normalizeForWordle()
                    if (t.isBlank()) null
                    else WordEntity(
                        id       = item.id,
                        text     = t,
                        language = language,
                        length   = wordLength,
                    )
                }
                if (entities.isNotEmpty()) {
                    db.wordDao().deleteWords(language, wordLength)
                    db.wordDao().insertWords(entities)
                    return entities.map { it.text }
                }
                // API returned rows but no parseable text (e.g. wrong JSON shape) — drop stale cache
                db.wordDao().deleteWords(language, wordLength)
            }
            val cached = db.wordDao().getWords(language, wordLength)
            cached.map { it.text.normalizeForWordle() }
        } catch (e: Exception) {
            val cached = db.wordDao().getWords(language, wordLength)
            if (cached.isNotEmpty()) cached.map { it.text.normalizeForWordle() }
            else throw e
        }
    }
}