package com.wordle.game.data.di

import com.wordle.game.data.remote.api.GameApiService
import com.wordle.game.data.remote.datasource.game.GameRemoteDataSource
import com.wordle.game.data.remote.datasource.game.GameRemoteDataSourceImpl
import com.wordle.game.domain.repository.GameRepository
import com.wordle.game.domain.usecases.game.GetWordsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
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
        .baseUrl("http://192.168.100.168:1337/api/")
//        .baseUrl("http://10.0.2.2:1337/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideGameApiService(retrofit: Retrofit): GameApiService =
        retrofit.create(GameApiService::class.java)

}