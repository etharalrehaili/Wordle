package com.khammin.core.domain.usecase

import com.khammin.core.domain.model.LanguageModel
import com.khammin.core.domain.repository.LanguageRepository
import javax.inject.Inject

class GetLanguageUseCase @Inject constructor(
    private val repository: LanguageRepository
) {
    operator fun invoke(): List<LanguageModel> =
        repository.getLanguages()
}