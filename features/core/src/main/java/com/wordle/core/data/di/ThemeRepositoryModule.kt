package com.wordle.core.data.di

import com.wordle.core.data.repository.ThemeRepositoryImpl
import com.wordle.core.domain.repository.ThemeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemeRepositoryModule {
    @Singleton
    @Binds
    abstract fun bindsThemeRepository(themeRepositoryImpl: ThemeRepositoryImpl): ThemeRepository
}