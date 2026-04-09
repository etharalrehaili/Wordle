package com.khammin.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import com.khammin.core.data.extentions.gameProgressDataStore
import com.khammin.core.data.extentions.languageDataStore
import com.khammin.core.data.extentions.themeDataStore
import com.khammin.core.domain.model.GameProgress
import com.khammin.core.domain.model.LanguageModel
import com.khammin.core.domain.model.ThemeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideLanguageDataStore(
        @ApplicationContext context: Context
    ): DataStore<LanguageModel> = context.languageDataStore

    @Provides
    @Singleton
    fun provideThemeDataStore(
        @ApplicationContext context: Context
    ): DataStore<ThemeModel> = context.themeDataStore

    @Provides
    @Singleton
    fun provideGameProgressDataStore(
        @ApplicationContext context: Context
    ): DataStore<GameProgress> = context.gameProgressDataStore
}