package com.wordle.game.data.di

import android.content.Context
import androidx.room.Room
import com.wordle.game.data.local.dao.WordDAO
import com.wordle.game.data.local.db.AppDatabase
import com.wordle.game.data.remote.datasource.game.GameRemoteDataSource
import com.wordle.game.data.repository.GameRepositoryImpl
import com.wordle.game.domain.repository.GameRepository
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wordle_db")
            .fallbackToDestructiveMigration(false)
            .build()

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