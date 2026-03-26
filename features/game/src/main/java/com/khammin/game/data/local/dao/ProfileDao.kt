package com.khammin.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.khammin.game.data.local.entity.ProfileEntity

/**
 * DAO (Data Access Object) for local profile operations
 * Used by "ProfileRepositoryImpl" to cache and retrieve profile data offline
 */
@Dao
interface ProfileDao {


    // Inserts or replaces a profile in the local database -> Called after fetching or updating a profile from the remote API
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    // Returns the cached profile for the given Firebase UID, or null if not found.
    @Query("SELECT * FROM profile_table WHERE firebaseUid = :firebaseUid LIMIT 1")
    suspend fun getProfile(firebaseUid: String): ProfileEntity?

    // Deletes the cached profile for the given Firebase UID -> Called on logout to clear user-specific data
    @Query("DELETE FROM profile_table WHERE firebaseUid = :firebaseUid")
    suspend fun deleteProfile(firebaseUid: String)
}