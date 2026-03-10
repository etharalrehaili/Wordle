package com.wordle.game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenge_table")
data class ChallengeEntity(
    @PrimaryKey val id: Int,
    val documentId: String,
    val word: String,
    val language: String,
    val date: String,
)