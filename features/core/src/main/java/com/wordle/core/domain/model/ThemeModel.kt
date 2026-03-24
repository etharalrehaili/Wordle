package com.wordle.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ThemeModel(
    val id: Int,
    val name: String,
)

val DARK_MODEL = ThemeModel(
    id   = 1,
    name = "DARK"
)

val LIGHT_MODEL = ThemeModel(
    id   = 2,
    name = "LIGHT"
)

fun ThemeModel.isDark() = name == "DARK"