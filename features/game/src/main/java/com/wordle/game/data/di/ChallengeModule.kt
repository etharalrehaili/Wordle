package com.wordle.game.data.di

import android.content.Context
import com.wordle.game.data.local.db.AppDatabase
import com.wordle.game.data.remote.api.ChallengeApiService
import com.wordle.game.data.remote.datasource.challenge.ChallengeRemoteDataSource
import com.wordle.game.data.remote.datasource.challenge.ChallengeRemoteDataSourceImpl
import com.wordle.game.data.repository.ChallengeRepositoryImpl
import com.wordle.game.domain.repository.ChallengeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChallengeModule {

    @Provides
    @Singleton
    fun provideChallengeApiService(retrofit: Retrofit): ChallengeApiService =
        retrofit.create(ChallengeApiService::class.java)

    @Provides
    @Singleton
    fun provideChallengeRemoteDataSource(api: ChallengeApiService): ChallengeRemoteDataSource =
        ChallengeRemoteDataSourceImpl(api)

    @Provides
    @Singleton
    fun provideChallengeRepository(
        @ApplicationContext context: Context,
        remote: ChallengeRemoteDataSource,
        db: AppDatabase,
    ): ChallengeRepository = ChallengeRepositoryImpl(context, remote, db)

}