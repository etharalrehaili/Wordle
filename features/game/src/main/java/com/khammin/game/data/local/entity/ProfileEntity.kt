package com.khammin.game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local database entity representing a cached user profile -> same as "ProfileItem" but stored in Room for offline access
 */

@Entity(tableName = "profile_table")
data class ProfileEntity(
    @PrimaryKey val firebaseUid: String, // Firebase UID used as the unique identifier for this profile
    val documentId: String, // Strapi document ID used for remote update/delete operations
    val name: String,
    val avatarUrl: String?,
    val enGamesPlayed: Int,
    val arGamesPlayed: Int,
    val enWordsSolved: Int,
    val arWordsSolved: Int,
    val enWinPercentage: Double, // (wordsSolved / gamesPlayed) * 100
    val arWinPercentage: Double, // (wordsSolved / gamesPlayed) * 100
    val enCurrentPoints: Int,
    val arCurrentPoints: Int,
    val enLastPlayedAt: String? = null,
    val arLastPlayedAt: String? = null,
)