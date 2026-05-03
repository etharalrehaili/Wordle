package com.khammin.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.khammin.game.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profile_table WHERE firebaseUid = :firebaseUid LIMIT 1")
    suspend fun getProfile(firebaseUid: String): ProfileEntity?

    @Query("SELECT * FROM profile_table WHERE firebaseUid = :firebaseUid LIMIT 1")
    fun observeProfile(firebaseUid: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile_table WHERE pendingSync = 1")
    suspend fun getPendingSyncProfiles(): List<ProfileEntity>

    @Query("DELETE FROM profile_table WHERE firebaseUid = :firebaseUid")
    suspend fun deleteProfile(firebaseUid: String)
}
