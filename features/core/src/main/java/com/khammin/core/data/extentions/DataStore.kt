package com.khammin.core.data.extentions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.khammin.core.data.serializer.GameProgressSerializer
import com.khammin.core.data.serializer.LanguageSerializer
import com.khammin.core.data.serializer.ThemeSerializer
import com.khammin.core.domain.model.GameProgress
import com.khammin.core.domain.model.LanguageModel
import com.khammin.core.domain.model.ThemeModel

private const val LANGUAGE_STORE_FILE      = "language.json"
private const val THEME_STORE_FILE         = "theme.json"
private const val GAME_PROGRESS_STORE_FILE = "game_progress.json"

val Context.languageDataStore: DataStore<LanguageModel> by dataStore(
    fileName   = LANGUAGE_STORE_FILE,
    serializer = LanguageSerializer
)

val Context.themeDataStore: DataStore<ThemeModel> by dataStore(
    fileName   = THEME_STORE_FILE,
    serializer = ThemeSerializer
)

val Context.gameProgressDataStore: DataStore<GameProgress> by dataStore(
    fileName   = GAME_PROGRESS_STORE_FILE,
    serializer = GameProgressSerializer
)