package com.khammin.game.data.di

import com.google.firebase.firestore.FirebaseFirestore
import com.khammin.game.data.remote.datasource.challenge.ChallengeProgressDataSource
import com.khammin.game.data.remote.datasource.challenge.ChallengeProgressDataSourceImpl
import com.khammin.game.data.repository.ChallengeProgressRepositoryImpl
import com.khammin.game.domain.repository.ChallengeProgressRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChallengeProgressModule {

    @Provides
    @Singleton
    fun provideChallengeProgressDataSource(
        firestore: FirebaseFirestore,
    ): ChallengeProgressDataSource =
        ChallengeProgressDataSourceImpl(firestore)

    @Provides
    @Singleton
    fun provideChallengeProgressRepository(
        dataSource: ChallengeProgressDataSource,
    ): ChallengeProgressRepository =
        ChallengeProgressRepositoryImpl(dataSource)
}
