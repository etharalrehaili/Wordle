package com.khammin.game.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Device-level store for completed challenge IDs.
 *
 * Persists challenge completions across Firebase UID changes (guest → Google sign-in → logout).
 * This prevents the exploit where logging out resets Firestore progress and allows
 * a user to re-complete challenges (and re-earn points) on the same device.
 */
@Singleton
class CompletedChallengesStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs by lazy {
        context.getSharedPreferences("completed_challenges", Context.MODE_PRIVATE)
    }

    private val KEY = "ids"

    fun getAll(): Set<String> = prefs.getStringSet(KEY, emptySet()) ?: emptySet()

    fun markCompleted(ids: Collection<String>) {
        if (ids.isEmpty()) return
        val updated = getAll().toMutableSet().apply { addAll(ids) }
        prefs.edit().putStringSet(KEY, updated).apply()
    }

    fun contains(id: String): Boolean = id in getAll()
}
