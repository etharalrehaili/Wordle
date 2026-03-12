package com.wordle.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wordle.game.data.local.entity.ChallengeEntity

@Dao
interface ChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: ChallengeEntity)

    @Query("SELECT * FROM challenge_table WHERE date = :date AND language = :language")
    suspend fun getChallenge(date: String, language: String): ChallengeEntity?

    @Query("DELETE FROM challenge_table WHERE date < :date")
    suspend fun deleteOldChallenges(date: String)
}