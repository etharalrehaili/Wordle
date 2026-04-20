package com.khammin.game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local database entity representing a cached user profile.
 * [pendingSync] is true when the record was updated offline and has not yet
 * been pushed to the server. [pendingSyncLanguage] records which language's
 * stats were changed so the sync worker sends the right payload.
 */
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
