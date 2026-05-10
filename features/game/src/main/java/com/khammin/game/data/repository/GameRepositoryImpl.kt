package com.khammin.game.data.repository

import com.khammin.core.util.normalizeForWordle
import com.khammin.game.data.local.db.AppDatabase
import com.khammin.game.data.local.entity.WordEntity
import com.khammin.game.data.remote.datasource.game.GameRemoteDataSource
import com.khammin.game.data.remote.model.WordData
import com.khammin.game.data.remote.model.resolvedMeaning
import com.khammin.game.data.remote.model.resolvedText
import com.khammin.game.domain.repository.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor(
    private val remote: GameRemoteDataSource,
    private val db: AppDatabase,
) : GameRepository {

    override suspend fun getWords(language: String, wordLength: Int): List<WordData> {
        // Return cache immediately if available, then refresh in background
        val cached = db.wordDao().getWords(language, wordLength)
        // If cache exists but all rows have null meaning (stale cache from before meaning was added),
        // skip returning stale data and fall through to a fresh network fetch below.
        val cacheHasMeaning = cached.any { it.meaning != null }
        if (cached.isNotEmpty() && cacheHasMeaning) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { fetchAndCache(language, wordLength) }
            }
            return cached.map { WordData(it.text, it.meaning) }
        }
        // No cache — must wait for network (first launch or after DB wipe)
        return try {
            fetchAndCache(language, wordLength)
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun fetchAndCache(language: String, wordLength: Int): List<WordData> {
        val items = remote.getWords(language, wordLength)
        if (items.isEmpty()) return emptyList()
        val entities = items.mapNotNull { item ->
            val t = item.resolvedText().normalizeForWordle()
            if (t.isBlank()) null
            else WordEntity(id = item.id, text = t, language = language, length = wordLength, meaning = item.resolvedMeaning())
        }
        if (entities.isNotEmpty()) {
            db.wordDao().deleteWords(language, wordLength)
            db.wordDao().insertWords(entities)
        }
        return entities.map { WordData(it.text, it.meaning) }
    }

    override suspend fun validateWord(word: String, language: String): Boolean {
        return remote.validateWord(word, language)
    }
}