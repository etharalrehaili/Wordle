package com.khammin.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import com.khammin.core.domain.model.DARK_MODEL
import com.khammin.core.domain.model.LIGHT_MODEL
import com.khammin.core.domain.model.ThemeModel
import com.khammin.core.domain.repository.ThemeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val themeDataStore: DataStore<ThemeModel>
) : ThemeRepository {

    // In-memory cache: populated on first read, updated on every write.
    // Eliminates repeated runBlocking disk I/O on the main thread.
    @Volatile private var cached: ThemeModel? = null

    override fun getThemes(): List<ThemeModel> = listOf(DARK_MODEL, LIGHT_MODEL)

    override fun setTheme(theme: ThemeModel) {
        cached = theme
        // Fire-and-forget — UI consistency is guaranteed by the in-memory cache.
        CoroutineScope(Dispatchers.IO).launch {
            themeDataStore.updateData { theme }
        }
    }

    override fun getCurrentTheme(): ThemeModel {
        return cached ?: runBlocking { themeDataStore.data.first() }.also { cached = it }
    }
}