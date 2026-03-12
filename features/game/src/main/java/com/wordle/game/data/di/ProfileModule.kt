package com.wordle.game.data.di

import com.wordle.game.data.local.db.AppDatabase
import com.wordle.game.data.remote.api.ProfileApiService
import com.wordle.game.data.remote.datasource.profile.ProfileRemoteDataSource
import com.wordle.game.data.remote.datasource.profile.ProfileRemoteDataSourceImpl
import com.wordle.game.data.repository.ProfileRepositoryImpl
import com.wordle.game.domain.repository.ProfileRepository
import com.wordle.game.domain.usecases.leaderboard.GetLeaderboardUseCase
import com.wordle.game.domain.usecases.profile.CreateProfileUseCase
import com.wordle.game.domain.usecases.profile.GetProfileUseCase
import com.wordle.game.domain.usecases.profile.UpdateProfileUseCase
import com.wordle.game.domain.usecases.profile.UploadAvatarUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt DI module for the profile feature.
 * Provides all profile-related dependencies: API service, data sources,
 * repository, and use cases.
 */
@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    /** Provides the Retrofit API service for profile-related endpoints. */
    @Provides @Singleton
    fun provideProfileApiService(retrofit: Retrofit): ProfileApiService =
        retrofit.create(ProfileApiService::class.java)

    /** Provides the remote data source that communicates with the Strapi API. */
    @Provides @Singleton
    fun provideProfileRemoteDataSource(api: ProfileApiService): ProfileRemoteDataSource =
        ProfileRemoteDataSourceImpl(api)

    /**
     * Provides the profile repository with both remote and local (Room) data sources.
     * Implements a cache-first strategy: local DB is checked before hitting the API.
     */
    @Provides @Singleton
    fun provideProfileRepository(
        remote: ProfileRemoteDataSource,
        db: AppDatabase,
    ): ProfileRepository = ProfileRepositoryImpl(remote, db)

    /** Provides the use case for fetching a user profile by Firebase UID. */
    @Provides @Singleton
    fun provideGetProfileUseCase(repository: ProfileRepository): GetProfileUseCase =
        GetProfileUseCase(repository)

    /** Provides the use case for creating a new profile on first login. */
    @Provides @Singleton
    fun provideCreateProfileUseCase(repository: ProfileRepository): CreateProfileUseCase =
        CreateProfileUseCase(repository)

    /** Provides the use case for updating profile info and game stats. */
    @Provides @Singleton
    fun provideUpdateProfileUseCase(repo: ProfileRepository): UpdateProfileUseCase =
        UpdateProfileUseCase(repo)

    /** Provides the use case for uploading a profile avatar image to Strapi. */
    @Provides @Singleton
    fun provideUploadAvatarUseCase(repo: ProfileRepository): UploadAvatarUseCase =
        UploadAvatarUseCase(repo)

    /** Provides the use case for fetching the top players leaderboard. */
    @Provides @Singleton
    fun provideGetLeaderboardUseCase(repo: ProfileRepository): GetLeaderboardUseCase =
        GetLeaderboardUseCase(repo)
}