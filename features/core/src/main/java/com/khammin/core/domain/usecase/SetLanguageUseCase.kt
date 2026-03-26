package com.khammin.core.domain.usecase

import com.khammin.core.domain.model.LanguageModel
import com.khammin.core.domain.repository.LanguageRepository
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val repository: LanguageRepository
) {
    operator fun invoke(language: LanguageModel) = repository.setLanguage(language)
}