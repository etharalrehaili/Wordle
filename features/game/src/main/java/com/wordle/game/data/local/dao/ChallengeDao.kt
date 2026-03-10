package com.wordle.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.wordle.game.data.local.entity.ChallengeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Insert
    suspend fun insertChallenge(challenge: ChallengeEntity)

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Query("SELECT * FROM challenge_table WHERE id = :id")
    fun getChallengeById(id: Int): Flow<ChallengeEntity?>

    @Query("SELECT * FROM challenge_table")
    fun getAllChallenges(): Flow<List<ChallengeEntity>>
}