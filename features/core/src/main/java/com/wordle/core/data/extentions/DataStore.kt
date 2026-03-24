package com.wordle.core.data.extentions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.wordle.core.data.serializer.LanguageSerializer
import com.wordle.core.data.serializer.ThemeSerializer
import com.wordle.core.domain.model.LanguageModel
import com.wordle.core.domain.model.ThemeModel

val Context.languageDataStore: DataStore<LanguageModel> by dataStore(
    fileName   = "language.json",
    serializer = LanguageSerializer
)

val Context.themeDataStore: DataStore<ThemeModel> by dataStore(
    fileName   = "theme.json",
    serializer = ThemeSerializer
)