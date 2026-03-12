package com.wordle.game.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "challenge_table",
    primaryKeys = ["date", "language"]
)
data class ChallengeEntity(
    val date: String,
    val language: String,
    val word: String,
)