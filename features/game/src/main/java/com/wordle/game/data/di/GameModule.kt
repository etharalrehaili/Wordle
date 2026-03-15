package com.wordle.game.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.wordle.game.data.remote.api.GameApiService
import com.wordle.game.data.remote.datasource.game.GameRemoteDataSource
import com.wordle.game.data.remote.datasource.game.GameRemoteDataSourceImpl
import com.wordle.game.domain.repository.GameRepository
import com.wordle.game.domain.usecases.game.GetWordsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
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
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.100.227:1337/api/")
//        .baseUrl("http://10.0.2.2:1337/api/") // Use this for Android emulator
        .addConverterFactory(GsonConverterFactory.create())
        .build()

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

}