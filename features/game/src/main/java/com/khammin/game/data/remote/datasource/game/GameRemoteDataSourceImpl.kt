package com.khammin.game.data.remote.datasource.game

import com.khammin.game.data.remote.api.GameApiService
import com.khammin.game.data.remote.model.ValidateWordRequest
import com.khammin.game.data.remote.model.WordItem
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

    override suspend fun validateWord(word: String, language: String): Boolean {
        val request = ValidateWordRequest(word = word, language = language)
        val response = api.validateWord(request)
        return response.isValid
    }

    private companion object {
        private const val PAGE_SIZE = 100
        private const val MAX_PAGES = 500
    }
}