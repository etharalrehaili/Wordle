package com.wordle.game.data.remote.api

import com.wordle.game.data.remote.model.WordResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GameApiService {
    @GET("words")
    suspend fun getWords(
        @Query("filters[language][\$eq]") language: String,
        @Query("filters[length][\$eq]") length: Int,
        /** Strapi often caps pageSize (e.g. 100); [GameRemoteDataSourceImpl] requests all pages. */
        @Query("pagination[page]") page: Int = 1,
        @Query("pagination[pageSize]") pageSize: Int = 100,
        @Query("fields[0]") field0: String = "text",
        @Query("fields[1]") field1: String = "language",
        @Query("fields[2]") field2: String = "length",
    ): WordResponse
}