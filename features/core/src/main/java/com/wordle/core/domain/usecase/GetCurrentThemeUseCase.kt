package com.wordle.core.domain.usecase

import com.wordle.core.domain.model.ThemeModel
import com.wordle.core.domain.repository.ThemeRepository
import javax.inject.Inject

class GetCurrentThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    operator fun invoke(): ThemeModel = repository.getCurrentTheme()
}