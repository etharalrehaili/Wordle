package com.khammin.game.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.khammin.game.data.local.GuestProfileDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GuestProfileStore

private val Context.guestProfileDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "guest_profile"
)

@Module
@InstallIn(SingletonComponent::class)
object GuestProfileModule {

    @Provides
    @Singleton
    @GuestProfileStore
    fun provideGuestProfilePreferences(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.guestProfileDataStore

    @Provides
    @Singleton
    fun provideGuestProfileDataStore(
        @GuestProfileStore dataStore: DataStore<Preferences>
    ): GuestProfileDataStore = GuestProfileDataStore(dataStore)
}
