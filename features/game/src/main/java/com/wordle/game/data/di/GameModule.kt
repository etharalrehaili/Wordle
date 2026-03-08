package com.wordle.game.data.di

import android.content.Context
import com.wordle.game.data.remote.datasource.GameRemoteDataSource
import com.wordle.game.data.remote.datasource.GameRemoteDataSourceImpl
import com.wordle.game.data.repository.ChallengeRepositoryImpl
import com.wordle.game.data.repository.GameRepositoryImpl
import com.wordle.game.domain.repository.ChallengeRepository
import com.wordle.game.domain.repository.GameRepository
import com.wordle.game.domain.usecases.GetWordsUseCase
import com.wordle.game.domain.usecases.LoadTodayChallengeUseCase
import com.wordle.game.domain.usecases.SaveChallengeStateUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameModule {

    @Provides
    @Singleton
    fun provideGameRemoteDataSource(
        @ApplicationContext context: Context
    ): GameRemoteDataSource = GameRemoteDataSourceImpl(context)

    @Provides
    @Singleton
    fun provideGameRepository(
        remote: GameRemoteDataSource
    ): GameRepository = GameRepositoryImpl(remote)

    @Provides
    @Singleton
    fun provideGetWordsUseCase(
        repository: GameRepository
    ): GetWordsUseCase = GetWordsUseCase(repository)

    @Provides
    @Singleton
    fun provideChallengeRepository(
        @ApplicationContext context: Context
    ): ChallengeRepository = ChallengeRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideLoadTodayChallengeUseCase(
        repository: ChallengeRepository
    ): LoadTodayChallengeUseCase = LoadTodayChallengeUseCase(repository)

    @Provides
    @Singleton
    fun provideSaveChallengeStateUseCase(
        repository: ChallengeRepository
    ): SaveChallengeStateUseCase = SaveChallengeStateUseCase(repository)
}