package com.wordle.game.data.local.db

import com.wordle.game.data.local.entity.WordEntity
import androidx.room.Database
import androidx.room.RoomDatabase
import com.wordle.game.data.local.dao.ChallengeDao
import com.wordle.game.data.local.dao.ProfileDao
import com.wordle.game.data.local.dao.WordDAO
import com.wordle.game.data.local.entity.ChallengeEntity
import com.wordle.game.data.local.entity.ProfileEntity

@Database(
    entities = [WordEntity::class, ChallengeEntity::class, ProfileEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDAO
    abstract fun challengeDao(): ChallengeDao
    abstract fun profileDao(): ProfileDao
}