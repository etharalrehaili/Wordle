package com.khammin.core.domain.repository

import com.khammin.core.domain.model.LanguageModel

interface LanguageRepository {
    fun getLanguages(): List<LanguageModel>
    fun setLanguage(language: LanguageModel)
    fun getCurrentLanguage(): LanguageModel
}