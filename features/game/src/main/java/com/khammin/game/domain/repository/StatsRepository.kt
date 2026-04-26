package com.khammin.game.domain.repository

interface StatsRepository {
    suspend fun recordGame(language: String, isWin: Boolean)
}
