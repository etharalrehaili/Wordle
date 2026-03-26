package com.khammin.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import com.khammin.core.domain.model.DARK_MODEL
import com.khammin.core.domain.model.LIGHT_MODEL
import com.khammin.core.domain.model.ThemeModel
import com.khammin.core.domain.repository.ThemeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val themeDataStore: DataStore<ThemeModel>
) : ThemeRepository {

    override fun getThemes(): List<ThemeModel> = listOf(DARK_MODEL, LIGHT_MODEL)

    override fun setTheme(theme: ThemeModel) {
        runBlocking { themeDataStore.updateData { theme } }
    }

    override fun getCurrentTheme(): ThemeModel {
        return runBlocking { themeDataStore.data.first() }
    }
}