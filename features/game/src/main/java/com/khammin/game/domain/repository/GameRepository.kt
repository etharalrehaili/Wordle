package com.khammin.game.domain.repository

interface GameRepository {
    suspend fun getWords(language: String, wordLength: Int): List<String>
}