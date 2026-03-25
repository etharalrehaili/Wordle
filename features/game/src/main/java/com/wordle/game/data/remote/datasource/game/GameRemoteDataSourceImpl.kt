package com.wordle.game.data.remote.datasource.game

import com.wordle.game.data.remote.api.GameApiService
import com.wordle.game.data.remote.model.WordItem
import javax.inject.Inject

class GameRemoteDataSourceImpl @Inject constructor(
    private val api: GameApiService
) : GameRemoteDataSource {

    override suspend fun getWords(language: String, wordLength: Int): List<WordItem> {
        val all = mutableListOf<WordItem>()
        var page = 1
        while (page <= MAX_PAGES) {
            val response = api.getWords(language, wordLength, page = page)
            if (response.data.isEmpty()) break
            all.addAll(response.data)
            val pageCount = response.meta?.pagination?.pageCount?.takeIf { it > 0 }
            val lastPage = when {
                pageCount != null -> page >= pageCount
                else -> response.data.size < PAGE_SIZE
            }
            if (lastPage) break
            page++
        }
        return all
    }

    private companion object {
        /** Must match [GameApiService.getWords] default `pagination[pageSize]`. */
        private const val PAGE_SIZE = 100
        private const val MAX_PAGES = 500
    }
}