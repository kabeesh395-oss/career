package com.example.careerpilot.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {

    companion object {
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val TARGET_ROLE = stringPreferencesKey("target_role")
        val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        val AUTO_SYNC_ENABLED = booleanPreferencesKey("auto_sync_enabled")
        val CUSTOM_GEMINI_API_KEY = stringPreferencesKey("custom_gemini_api_key")
    }

    val customGeminiApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CUSTOM_GEMINI_API_KEY] ?: ""
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE_ENABLED] ?: true
    }

    val targetRole: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[TARGET_ROLE] ?: "Full Stack Engineer"
    }

    val isOfflineMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[OFFLINE_MODE] ?: false
    }

    val lastSyncTimestamp: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[LAST_SYNC_TIMESTAMP] ?: 0L
    }

    val isAutoSyncEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AUTO_SYNC_ENABLED] ?: true
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun setTargetRole(role: String) {
        context.dataStore.edit { prefs ->
            prefs[TARGET_ROLE] = role
        }
    }

    suspend fun setOfflineMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[OFFLINE_MODE] = enabled
        }
    }

    suspend fun updateLastSyncTimestamp(timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            prefs[LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_SYNC_ENABLED] = enabled
        }
    }

    suspend fun setCustomGeminiApiKey(apiKey: String) {
        context.dataStore.edit { prefs ->
            prefs[CUSTOM_GEMINI_API_KEY] = apiKey.trim()
        }
    }
}
