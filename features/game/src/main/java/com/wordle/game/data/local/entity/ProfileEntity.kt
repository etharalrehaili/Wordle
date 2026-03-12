package com.wordle.game.data.local.entity

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
    val gamesPlayed: Int,
    val wordsSolved: Int,
    val winPercentage: Double, // (wordsSolved / gamesPlayed) * 100
    val currentPoints: Int,
)