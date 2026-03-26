package com.khammin.core.domain.repository

import com.khammin.core.domain.model.ThemeModel

interface ThemeRepository {
    fun getThemes(): List<ThemeModel>
    fun setTheme(language: ThemeModel)
    fun getCurrentTheme(): ThemeModel
}