package com.khammin.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import com.khammin.core.domain.model.ARABIC_MODEL
import com.khammin.core.domain.model.ENGLISH_MODEL
import com.khammin.core.domain.model.LanguageModel
import com.khammin.core.domain.repository.LanguageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val languageDataStore: DataStore<LanguageModel>
) : LanguageRepository {

    override fun getLanguages(): List<LanguageModel> = listOf(ENGLISH_MODEL, ARABIC_MODEL)

    override fun setLanguage(language: LanguageModel) {
        runBlocking { languageDataStore.updateData { language } }
    }

    override fun getCurrentLanguage(): LanguageModel {
        return runBlocking { languageDataStore.data.first() }
    }
}