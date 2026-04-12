package com.khammin.game.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.firebase.database.FirebaseDatabase
import com.khammin.game.data.remote.datasource.game.GameRemoteDataSource
import com.khammin.game.data.remote.datasource.game.GameRemoteDataSourceImpl
import com.khammin.game.domain.repository.GameRepository
import com.khammin.game.domain.usecases.game.GetWordsUseCase
import com.khammin.game.domain.usecases.game.ValidateWordUseCase
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
import okhttp3.logging.HttpLoggingInterceptor
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
    fun provideValidateWordUseCase(
        repository: GameRepository
    ): ValidateWordUseCase = ValidateWordUseCase(repository)

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer 1120ff8946655caf08da294d425ff7b3174d059d66c76d1727b2dbb08e02e3dac380bae98654c67a5d7b8d8c252a1deda3b1d8c96c1dd77888d971ba48e830e64f076f90fbf0fc9abd2346350a68a15e1f81131085f21ca2818e9c09fad97f8b5f807eb069fc09ffca4eb267a633e02ef13e349b242fbc24426a7074a88a76a0")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
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

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase =
        FirebaseDatabase.getInstance("https://khammin-default-rtdb.asia-southeast1.firebasedatabase.app")

}