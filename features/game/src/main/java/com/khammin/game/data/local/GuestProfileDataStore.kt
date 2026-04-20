package com.khammin.game.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class GuestProfileData(
    val name: String,
    val avatarColor: Long?,
    val avatarEmoji: String?,
)

class GuestProfileDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_HAS_PROFILE  = booleanPreferencesKey("guest_has_profile")
        val KEY_NAME         = stringPreferencesKey("guest_name")
        val KEY_AVATAR_COLOR = longPreferencesKey("guest_avatar_color")
        val KEY_AVATAR_EMOJI = stringPreferencesKey("guest_avatar_emoji")
    }

    suspend fun getProfile(): GuestProfileData? {
        val prefs = dataStore.data.first()
        if (prefs[KEY_HAS_PROFILE] != true) return null
        return GuestProfileData(
            name        = prefs[KEY_NAME] ?: return null,
            avatarColor = prefs[KEY_AVATAR_COLOR],
            avatarEmoji = prefs[KEY_AVATAR_EMOJI],
        )
    }

    suspend fun saveProfile(name: String, avatarColor: Long?, avatarEmoji: String?) {
        dataStore.edit { prefs ->
            prefs[KEY_HAS_PROFILE] = true
            prefs[KEY_NAME]        = name
            if (avatarColor != null) prefs[KEY_AVATAR_COLOR] = avatarColor
            else prefs.remove(KEY_AVATAR_COLOR)
            if (avatarEmoji != null) prefs[KEY_AVATAR_EMOJI] = avatarEmoji
            else prefs.remove(KEY_AVATAR_EMOJI)
        }
    }
}
