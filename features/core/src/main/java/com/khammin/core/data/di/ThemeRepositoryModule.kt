package com.khammin.core.data.di

import com.khammin.core.data.repository.ThemeRepositoryImpl
import com.khammin.core.domain.repository.ThemeRepository
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