package com.wordle.core.domain.repository

import com.wordle.core.domain.model.ThemeModel

interface ThemeRepository {
    fun getThemes(): List<ThemeModel>
    fun setTheme(language: ThemeModel)
    fun getCurrentTheme(): ThemeModel
}