package com.khammin.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import com.khammin.core.domain.model.ARABIC_MODEL
import com.khammin.core.domain.model.ENGLISH_MODEL
import com.khammin.core.domain.model.LanguageModel
import com.khammin.core.domain.repository.LanguageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val languageDataStore: DataStore<LanguageModel>
) : LanguageRepository {

    // In-memory cache: populated on first read, updated on every write.
    // Eliminates repeated runBlocking disk I/O on the main thread.
    @Volatile private var cached: LanguageModel? = null

    override fun getLanguages(): List<LanguageModel> = listOf(ENGLISH_MODEL, ARABIC_MODEL)

    override fun setLanguage(language: LanguageModel) {
        cached = language
        // Fire-and-forget — UI consistency is guaranteed by the in-memory cache.
        CoroutineScope(Dispatchers.IO).launch {
            languageDataStore.updateData { language }
        }
    }

    override fun getCurrentLanguage(): LanguageModel {
        return cached ?: runBlocking { languageDataStore.data.first() }.also { cached = it }
    }
}