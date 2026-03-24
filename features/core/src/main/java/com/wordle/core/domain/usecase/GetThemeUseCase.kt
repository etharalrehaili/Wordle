package com.wordle.core.domain.usecase

import com.wordle.core.domain.model.ThemeModel
import com.wordle.core.domain.repository.ThemeRepository
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    operator fun invoke(): List<ThemeModel> =
        repository.getThemes()
}