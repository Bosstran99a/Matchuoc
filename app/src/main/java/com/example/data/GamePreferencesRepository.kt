package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.mahjongDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mahjong_prefs",
)

data class PlayerPreferences(
    val highestUnlockedLevel: Int = 1,
    val bestScore: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val starsByLevel: Map<Int, Int> = emptyMap(),
)

class GamePreferencesRepository(private val context: Context) {

    private object Keys {
        val HIGHEST_LEVEL = intPreferencesKey("highest_unlocked_level")
        val BEST_SCORE = intPreferencesKey("best_score")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val LEVEL_STARS = stringPreferencesKey("level_stars_map")
    }

    val preferencesFlow: Flow<PlayerPreferences> = context.mahjongDataStore.data.map { prefs ->
        PlayerPreferences(
            highestUnlockedLevel = prefs[Keys.HIGHEST_LEVEL] ?: 1,
            bestScore = prefs[Keys.BEST_SCORE] ?: 0,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hapticEnabled = prefs[Keys.HAPTIC_ENABLED] ?: true,
            starsByLevel = decodeStarsMap(prefs[Keys.LEVEL_STARS].orEmpty()),
        )
    }

    suspend fun recordLevelCleared(levelNumber: Int, starsEarned: Int, score: Int) {
        context.mahjongDataStore.edit { prefs ->
            val currentHighest = prefs[Keys.HIGHEST_LEVEL] ?: 1
            if (levelNumber + 1 > currentHighest) {
                prefs[Keys.HIGHEST_LEVEL] = levelNumber + 1
            }
            val currentBest = prefs[Keys.BEST_SCORE] ?: 0
            if (score > currentBest) {
                prefs[Keys.BEST_SCORE] = score
            }
            val starsMap = decodeStarsMap(prefs[Keys.LEVEL_STARS].orEmpty()).toMutableMap()
            val prevStars = starsMap[levelNumber] ?: 0
            if (starsEarned > prevStars) {
                starsMap[levelNumber] = starsEarned
                prefs[Keys.LEVEL_STARS] = encodeStarsMap(starsMap)
            }
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.mahjongDataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.mahjongDataStore.edit { prefs ->
            prefs[Keys.HAPTIC_ENABLED] = enabled
        }
    }

    private fun decodeStarsMap(raw: String): Map<Int, Int> {
        if (raw.isBlank()) return emptyMap()
        return raw.split(",")
            .mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val lvl = parts[0].toIntOrNull()
                    val stars = parts[1].toIntOrNull()
                    if (lvl != null && stars != null) lvl to stars else null
                } else {
                    null
                }
            }
            .toMap()
    }

    private fun encodeStarsMap(map: Map<Int, Int>): String {
        return map.entries.joinToString(",") { "${it.key}:${it.value}" }
    }
}
