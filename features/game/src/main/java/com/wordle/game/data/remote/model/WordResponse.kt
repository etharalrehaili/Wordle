package com.wordle.game.data.remote.model

data class WordResponse(
    val data: List<WordItem>
)

data class WordItem(
    val id: Int,
    val text: String,
    val language: String,
    val length: Int
)