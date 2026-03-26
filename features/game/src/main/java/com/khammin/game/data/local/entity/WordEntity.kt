package com.khammin.game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_table")
data class WordEntity(
    @PrimaryKey val id: Int,
    val text: String,
    val language: String,
    val length: Int
)