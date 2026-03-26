package com.khammin.game.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.khammin.game.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Query("SELECT * FROM word_table WHERE language = :language AND length = :length")
    suspend fun getWords(language: String, length: Int): List<WordEntity>

    @Query("DELETE FROM word_table WHERE language = :language AND length = :length")
    suspend fun deleteWords(language: String, length: Int)

    @Query("SELECT COUNT(*) FROM word_table WHERE language = :language AND length = :length")
    suspend fun getWordCount(language: String, length: Int): Int

    @Query("SELECT * FROM word_table WHERE id = :id")
    fun getWordById(id: Int): Flow<WordEntity?>

    @Query("SELECT * FROM word_table")
    fun getAllWords(): Flow<List<WordEntity>>
}