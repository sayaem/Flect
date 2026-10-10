package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flect_preferences")

class PreferencesManager(private val context: Context) {

    private val favoritesKey = stringSetPreferencesKey("favorite_presets")
    private val recentsKey = stringPreferencesKey("recent_presets_csv")
    private val adaptiveDefaultKey = booleanPreferencesKey("adaptive_default_enabled")
    private val exportQualityKey = intPreferencesKey("export_jpeg_quality")

    val favoritePresetsFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[favoritesKey] ?: setOf("golden_hour", "soft_film", "cinemood")
    }

    val recentPresetsFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val csv = preferences[recentsKey] ?: ""
        if (csv.isBlank()) emptyList() else csv.split(",").filter { it.isNotBlank() }
    }

    val adaptiveDefaultFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[adaptiveDefaultKey] ?: true
    }

    val exportQualityFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[exportQualityKey] ?: 92
    }

    suspend fun toggleFavorite(presetId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[favoritesKey] ?: setOf("golden_hour", "soft_film", "cinemood")
            val updated = if (current.contains(presetId)) {
                current - presetId
            } else {
                current + presetId
            }
            preferences[favoritesKey] = updated
        }
    }

    suspend fun addRecentPreset(presetId: String) {
        context.dataStore.edit { preferences ->
            val currentCsv = preferences[recentsKey] ?: ""
            val list = currentCsv.split(",").filter { it.isNotBlank() && it != presetId }.toMutableList()
            list.add(0, presetId)
            val trimmed = list.take(10)
            preferences[recentsKey] = trimmed.joinToString(",")
        }
    }

    suspend fun setAdaptiveDefault(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[adaptiveDefaultKey] = enabled
        }
    }

    suspend fun setExportQuality(quality: Int) {
        context.dataStore.edit { preferences ->
            preferences[exportQualityKey] = quality.coerceIn(70, 100)
        }
    }
}
