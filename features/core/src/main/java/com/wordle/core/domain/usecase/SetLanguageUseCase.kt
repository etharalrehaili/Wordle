package com.wordle.core.domain.usecase

import com.wordle.core.domain.model.LanguageModel
import com.wordle.core.domain.repository.LanguageRepository
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val repository: LanguageRepository
) {
    operator fun invoke(language: LanguageModel) = repository.setLanguage(language)
}