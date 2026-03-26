package com.khammin.core.domain.usecase

import com.khammin.core.domain.model.ThemeModel
import com.khammin.core.domain.repository.ThemeRepository
import javax.inject.Inject

class GetCurrentThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    operator fun invoke(): ThemeModel = repository.getCurrentTheme()
}