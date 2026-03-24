package com.wordle.core.domain.usecase

import com.wordle.core.domain.model.LanguageModel
import com.wordle.core.domain.repository.LanguageRepository
import javax.inject.Inject

class GetLanguageUseCase @Inject constructor(
    private val repository: LanguageRepository
) {
    operator fun invoke(): List<LanguageModel> =
        repository.getLanguages()
}