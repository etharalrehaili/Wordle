package com.khammin.game.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.khammin.game.data.local.dao.ChallengeDao
import com.khammin.game.data.local.dao.ProfileDao
import com.khammin.game.data.local.dao.WordDAO
import com.khammin.game.data.local.entity.ChallengeEntity
import com.khammin.game.data.local.entity.ProfileEntity
import com.khammin.game.data.local.entity.WordEntity

@Database(
    entities = [WordEntity::class, ChallengeEntity::class, ProfileEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDAO
    abstract fun challengeDao(): ChallengeDao
    abstract fun profileDao(): ProfileDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE profile_table ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE profile_table ADD COLUMN pendingSyncLanguage TEXT"
                )
            }
        }

        // Added meaning column to WordEntity
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE word_table ADD COLUMN meaning TEXT"
                )
            }
        }
    }
}
