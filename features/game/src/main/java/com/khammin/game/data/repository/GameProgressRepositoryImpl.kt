package com.khammin.game.data.repository

import androidx.datastore.core.DataStore
import com.khammin.core.domain.model.GameProgress
import com.khammin.game.domain.repository.GameProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GameProgressRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<GameProgress>
) : GameProgressRepository {

    override fun getProgress(): Flow<GameProgress> = dataStore.data

    override suspend fun recordWin(wordLength: Int) {
        dataStore.updateData { current ->
            when (wordLength) {
                4    -> current.copy(easyWordsSolved    = current.easyWordsSolved + 1)
                5    -> current.copy(classicWordsSolved = current.classicWordsSolved + 1)
                else -> current
            }
        }
    }
}
