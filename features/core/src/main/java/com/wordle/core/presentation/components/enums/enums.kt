package com.wordle.core.presentation.components.enums

enum class Types {
    CORRECT, // the letter is in the correct position
    PRESENT, // the letter is in the word but in the wrong position
    ABSENT,  // the letter is not in the word
    DEFAULT  // default state for empty
}

enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    ARABIC("ar")
}

enum class AppColorTheme { DARK, LIGHT }

enum class DrawerScreen { MENU, THEME, LANGUAGE }