package com.khammin.game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_table")
data class ProfileEntity(
    @PrimaryKey val firebaseUid: String,
    val documentId: String,
    val name: String,
    val avatarUrl: String?,
    val arGamesPlayed: Int,
    val arWordsSolved: Int,
    val arWinPercentage: Double,
    val arCurrentPoints: Int,
    val arLastPlayedAt: String? = null,
    val pendingSync: Boolean = false,
    val pendingSyncLanguage: String? = null,
)
