package com.wordle.game.data.di

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

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    @Provides
    @Singleton
    fun provideGetProfileUseCase(
        repository: ProfileRepository
    ): GetProfileUseCase = GetProfileUseCase(repository)

    @Provides
    @Singleton
    fun provideCreateProfileUseCase(
        repository: ProfileRepository
    ): CreateProfileUseCase = CreateProfileUseCase(repository)

    @Provides @Singleton
    fun provideProfileApiService(retrofit: Retrofit): ProfileApiService =
        retrofit.create(ProfileApiService::class.java)

    @Provides @Singleton
    fun provideProfileRemoteDataSource(api: ProfileApiService): ProfileRemoteDataSource =
        ProfileRemoteDataSourceImpl(api)

    @Provides @Singleton
    fun provideProfileRepository(remote: ProfileRemoteDataSource): ProfileRepository =
        ProfileRepositoryImpl(remote)

    @Provides @Singleton
    fun provideUpdateProfileUseCase(repo: ProfileRepository): UpdateProfileUseCase =
        UpdateProfileUseCase(repo)

    @Provides @Singleton
    fun provideUploadAvatarUseCase(repo: ProfileRepository): UploadAvatarUseCase =
        UploadAvatarUseCase(repo)

    @Provides @Singleton
    fun provideGetLeaderboardUseCase(repo: ProfileRepository): GetLeaderboardUseCase =
        GetLeaderboardUseCase(repo)
}