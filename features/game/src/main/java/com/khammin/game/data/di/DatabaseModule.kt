package com.khammin.game.data.di

import android.content.Context
import androidx.room.Room
import com.khammin.game.data.local.dao.WordDAO
import com.khammin.game.data.local.db.AppDatabase
import com.khammin.game.data.local.secure.DatabaseEncryptionMigrator
import com.khammin.game.data.local.secure.SqlCipherKeyManager
import com.khammin.game.data.remote.datasource.game.GameRemoteDataSource
import com.khammin.game.data.repository.GameRepositoryImpl
import com.khammin.game.domain.repository.GameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        keyManager: SqlCipherKeyManager,
        migrator: DatabaseEncryptionMigrator,
    ): AppDatabase {
        migrator.migrateIfNeeded(context, "wordle_db")
        val factory = keyManager.getSupportFactory()
        if (keyManager.wasKeyRecovered) {
            context.deleteDatabase("wordle_db")
        }
        return Room.databaseBuilder(context, AppDatabase::class.java, "wordle_db")
            .openHelperFactory(factory)
            .addMigrations(AppDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideWordDao(db: AppDatabase): WordDAO = db.wordDao()

    @Provides
    @Singleton
    fun provideGameRepository(
        remote: GameRemoteDataSource,
        db: AppDatabase,
    ): GameRepository = GameRepositoryImpl(remote, db)
}
