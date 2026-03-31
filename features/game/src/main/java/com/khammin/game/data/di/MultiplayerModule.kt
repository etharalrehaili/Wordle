package com.khammin.game.data.di

import com.khammin.game.data.remote.datasource.game.MultiplayerDataSource
import com.khammin.game.data.remote.datasource.game.MultiplayerDataSourceImpl
import com.khammin.game.data.repository.MultiplayerRepositoryImpl
import com.khammin.game.domain.repository.MultiplayerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class MultiplayerModule {

    @Binds
    @Singleton
    abstract fun bindMultiplayerDataSource(
        impl: MultiplayerDataSourceImpl
    ): MultiplayerDataSource

    @Binds
    @Singleton
    abstract fun bindMultiplayerRepository(
        impl: MultiplayerRepositoryImpl
    ): MultiplayerRepository
}