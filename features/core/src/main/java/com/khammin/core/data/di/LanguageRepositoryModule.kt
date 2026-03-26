package com.khammin.core.data.di

import com.khammin.core.data.repository.LanguageRepositoryImpl
import com.khammin.core.domain.repository.LanguageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LanguageRepositoryModule {
    @Singleton
    @Binds
    abstract fun bindsLanguageRepository(languageRepositoryImpl: LanguageRepositoryImpl): LanguageRepository
}