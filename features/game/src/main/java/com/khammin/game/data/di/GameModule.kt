package com.khammin.game.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.khammin.game.data.remote.datasource.game.GameRemoteDataSource
import com.khammin.game.data.remote.datasource.game.GameRemoteDataSourceImpl
import com.khammin.game.domain.repository.GameRepository
import com.khammin.game.domain.usecases.game.GetWordsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.khammin.game.data.remote.api.GameApiService
import com.khammin.game.domain.repository.ProfileRepository
import com.khammin.game.domain.usecases.leaderboard.GetLeaderboardUseCase
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameModule {

    @Provides
    @Singleton
    fun provideGameRemoteDataSource(
        api: GameApiService
    ): GameRemoteDataSource = GameRemoteDataSourceImpl(api)

    @Provides
    @Singleton
    fun provideGetWordsUseCase(
        repository: GameRepository
    ): GetWordsUseCase = GetWordsUseCase(repository)

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer 1f93d59225275a11bb7f592dec1d55ad755c1ec8d612b7354c311e5b0099262666285b710057b4bae5a1c5b82dce8873ad4589b656dad44e4437b7d0b46ebb70e0c3b31336f1600a6859781f9672d6f30671547e44d9013da6d0bdffbfe70662ac0931e7e444f5b0f935c1c81724945fdb75c2f3c5c0d107809a285b70110d91")
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("https://khammin.com/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGameApiService(retrofit: Retrofit): GameApiService =
        retrofit.create(GameApiService::class.java)

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_preferences") }
        )

    /** Provides the use case for fetching the top players leaderboard. */
    @Provides @Singleton
    fun provideGetLeaderboardUseCase(repo: ProfileRepository): GetLeaderboardUseCase =
        GetLeaderboardUseCase(repo)

}