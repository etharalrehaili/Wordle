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
import com.khammin.core.data.ndk.KeyManager
import com.khammin.game.domain.repository.ProfileRepository
import com.khammin.game.domain.usecases.leaderboard.GetLeaderboardUseCase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val NETWORK_TIMEOUT_SECONDS = 30L
private const val USER_PREFERENCES_STORE_NAME = "user_preferences"

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
            .connectTimeout(NETWORK_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(NETWORK_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${KeyManager.getAuthToken()}")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(KeyManager.getBaseUrl())
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
            produceFile = { context.preferencesDataStoreFile(USER_PREFERENCES_STORE_NAME) }
        )

    /** Provides the use case for fetching the top players leaderboard. */
    @Provides @Singleton
    fun provideGetLeaderboardUseCase(repo: ProfileRepository): GetLeaderboardUseCase =
        GetLeaderboardUseCase(repo)

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase =
        FirebaseDatabase.getInstance(KeyManager.getFirebaseDbUrl())

}