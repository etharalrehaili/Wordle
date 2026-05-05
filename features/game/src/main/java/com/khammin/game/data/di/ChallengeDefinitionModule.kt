package com.khammin.game.data.di

import com.google.firebase.firestore.FirebaseFirestore
import com.khammin.game.data.remote.datasource.challenge.ChallengeDefinitionDataSource
import com.khammin.game.data.remote.datasource.challenge.ChallengeDefinitionDataSourceImpl
import com.khammin.game.data.repository.ChallengeDefinitionRepositoryImpl
import com.khammin.game.domain.repository.ChallengeDefinitionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChallengeDefinitionModule {

    @Provides
    @Singleton
    fun provideChallengeDefinitionDataSource(
        firestore: FirebaseFirestore,
    ): ChallengeDefinitionDataSource =
        ChallengeDefinitionDataSourceImpl(firestore)

    @Provides
    @Singleton
    fun provideChallengeDefinitionRepository(
        dataSource: ChallengeDefinitionDataSource,
    ): ChallengeDefinitionRepository =
        ChallengeDefinitionRepositoryImpl(dataSource)
}
