package com.khammin.core.domain.usecase

import com.khammin.core.domain.model.LanguageModel
import com.khammin.core.domain.repository.LanguageRepository
import javax.inject.Inject

class GetCurrentLanguageUseCase @Inject constructor(
    private val repository: LanguageRepository
) {
    operator fun invoke(): LanguageModel = repository.getCurrentLanguage()
}