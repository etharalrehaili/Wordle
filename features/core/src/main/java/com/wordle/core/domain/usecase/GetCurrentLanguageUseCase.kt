package com.wordle.core.domain.usecase

import com.wordle.core.domain.model.LanguageModel
import com.wordle.core.domain.repository.LanguageRepository
import javax.inject.Inject

class GetCurrentLanguageUseCase @Inject constructor(
    private val repository: LanguageRepository
) {
    operator fun invoke(): LanguageModel = repository.getCurrentLanguage()
}