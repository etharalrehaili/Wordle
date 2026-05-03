package com.khammin.game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_table")
data class ProfileEntity(
    @PrimaryKey val firebaseUid: String,
    val documentId: String,
    val name: String,
    val avatarUrl: String?,
    val enGamesPlayed: Int,
    val arGamesPlayed: Int,
    val enWordsSolved: Int,
    val arWordsSolved: Int,
    val enWinPercentage: Double,
    val arWinPercentage: Double,
    val enCurrentPoints: Int,
    val arCurrentPoints: Int,
    val enLastPlayedAt: String? = null,
    val arLastPlayedAt: String? = null,
    val pendingSync: Boolean = false,
    val pendingSyncLanguage: String? = null,
)
