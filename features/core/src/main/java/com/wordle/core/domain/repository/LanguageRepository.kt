package com.wordle.core.domain.repository

import com.wordle.core.domain.model.LanguageModel

interface LanguageRepository {
    fun getLanguages(): List<LanguageModel>
    fun setLanguage(language: LanguageModel)
    fun getCurrentLanguage(): LanguageModel
}