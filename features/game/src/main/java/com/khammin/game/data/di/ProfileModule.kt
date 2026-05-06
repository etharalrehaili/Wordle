package com.khammin.game.data.di

import android.content.ContentResolver
import android.content.Context
import androidx.work.WorkManager
import com.khammin.game.data.local.db.AppDatabase
import com.khammin.game.data.remote.api.ProfileApiService
import com.khammin.game.data.remote.datasource.profile.ProfileRemoteDataSource
import com.khammin.game.data.remote.datasource.profile.ProfileRemoteDataSourceImpl
import com.khammin.game.data.repository.ProfileRepositoryImpl
import com.khammin.game.domain.repository.ProfileRepository
import com.khammin.game.domain.usecases.profile.CreateProfileUseCase
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import com.khammin.game.domain.usecases.profile.UpdateProfileUseCase
import com.khammin.game.domain.usecases.profile.UploadAvatarUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    @Provides @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides @Singleton
    fun provideProfileApiService(retrofit: Retrofit): ProfileApiService =
        retrofit.create(ProfileApiService::class.java)

    @Provides @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
        context.contentResolver

    @Provides @Singleton
    fun provideProfileRemoteDataSource(
        api: ProfileApiService,
        contentResolver: ContentResolver,
    ): ProfileRemoteDataSource = ProfileRemoteDataSourceImpl(api, contentResolver)

    @Provides @Singleton
    fun provideProfileRepository(
        remote: ProfileRemoteDataSource,
        db: AppDatabase,
        workManager: WorkManager,
    ): ProfileRepository = ProfileRepositoryImpl(remote, db, workManager)

    @Provides @Singleton
    fun provideGetProfileUseCase(repository: ProfileRepository): GetProfileUseCase =
        GetProfileUseCase(repository)

    @Provides @Singleton
    fun provideCreateProfileUseCase(repository: ProfileRepository): CreateProfileUseCase =
        CreateProfileUseCase(repository)

    @Provides @Singleton
    fun provideUpdateProfileUseCase(repo: ProfileRepository): UpdateProfileUseCase =
        UpdateProfileUseCase(repo)

    @Provides @Singleton
    fun provideUploadAvatarUseCase(repo: ProfileRepository): UploadAvatarUseCase =
        UploadAvatarUseCase(repo)
}
