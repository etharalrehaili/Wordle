package com.khammin.game.data.di

import com.khammin.game.data.repository.GameProgressRepositoryImpl
import com.khammin.game.domain.repository.GameProgressRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GameProgressModule {

    @Binds
    @Singleton
    abstract fun bindGameProgressRepository(
        impl: GameProgressRepositoryImpl
    ): GameProgressRepository
}
