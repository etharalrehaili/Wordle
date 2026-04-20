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

val Context.languageDataStore: DataStore<LanguageModel> by dataStore(
    fileName   = "language.json",
    serializer = LanguageSerializer
)

val Context.themeDataStore: DataStore<ThemeModel> by dataStore(
    fileName   = "theme.json",
    serializer = ThemeSerializer
)

val Context.gameProgressDataStore: DataStore<GameProgress> by dataStore(
    fileName   = "game_progress.json",
    serializer = GameProgressSerializer
)