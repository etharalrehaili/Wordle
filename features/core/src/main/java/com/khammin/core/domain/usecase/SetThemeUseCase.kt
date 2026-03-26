package com.khammin.core.domain.usecase

import com.khammin.core.domain.model.ThemeModel
import com.khammin.core.domain.repository.ThemeRepository
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    operator fun invoke(theme: ThemeModel) = repository.setTheme(theme)
}