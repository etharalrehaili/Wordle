package com.wordle.game.data.remote.datasource

import android.content.Context
import com.wordle.core.presentation.components.WORD_LENGTH
import org.json.JSONObject
import javax.inject.Inject

class GameRemoteDataSourceImpl @Inject constructor(
    private val context: Context
) : GameRemoteDataSource {

    override suspend fun getWords(language: String): List<String> {
        val fileName = if (language == "ar") "arabWords.json" else "words.json"
        val json = context.assets.open(fileName).bufferedReader().readText()
        val array = JSONObject(json).getJSONArray("words")
        return List(array.length()) { i ->
            val word = array.getString(i)
            if (language == "ar") word else word.uppercase()
        }.filter { it.length == WORD_LENGTH }
    }
}