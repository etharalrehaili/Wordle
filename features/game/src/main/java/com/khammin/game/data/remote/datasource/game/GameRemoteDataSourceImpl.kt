package com.khammin.game.data.remote.datasource.game

import android.util.Log
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
        Log.d("WordValidation", "API_REQUEST word='${request.word}'  language='${request.language}'  unicode=${request.word.map { "U+%04X".format(it.code) }}")
        val response = api.validateWord(request)
        Log.d("WordValidation", "API_RESPONSE isValid=${response.isValid}")
        return response.isValid
    }

    private companion object {
        /** Must match [GameApiService.getWords] default `pagination[pageSize]`. */
        private const val PAGE_SIZE = 100
        private const val MAX_PAGES = 500
    }
}