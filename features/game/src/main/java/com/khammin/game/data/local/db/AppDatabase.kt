package com.khammin.game.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.khammin.game.data.local.dao.ChallengeDao
import com.khammin.game.data.local.dao.ProfileDao
import com.khammin.game.data.local.dao.WordDAO
import com.khammin.game.data.local.entity.ChallengeEntity
import com.khammin.game.data.local.entity.ProfileEntity
import com.khammin.game.data.local.entity.WordEntity

@Database(
    entities = [WordEntity::class, ChallengeEntity::class, ProfileEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDAO
    abstract fun challengeDao(): ChallengeDao
    abstract fun profileDao(): ProfileDao
}