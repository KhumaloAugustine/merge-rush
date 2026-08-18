package com.khumaloaugustine.mergerush

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("player_progress")

data class PlayerData(val highScore: Int = 0, val coins: Int = 0, val games: Int = 0, val highestTile: Int = 0, val unlockedLevel: Int = 1, val onboardingSeen: Boolean = false, val themeMode: String = "system")

class PlayerStore(private val context: Context) {
    private object Keys {
        val highScore = intPreferencesKey("high_score")
        val coins = intPreferencesKey("coins")
        val games = intPreferencesKey("games")
        val highestTile = intPreferencesKey("highest_tile")
        val unlockedLevel = intPreferencesKey("unlocked_level")
        val onboardingSeen = booleanPreferencesKey("onboarding_seen")
        val themeMode = stringPreferencesKey("theme_mode")
    }
    val data: Flow<PlayerData> = context.dataStore.data.map { p ->
        PlayerData(p[Keys.highScore] ?: 0, p[Keys.coins] ?: 0, p[Keys.games] ?: 0, p[Keys.highestTile] ?: 0, p[Keys.unlockedLevel] ?: 1, p[Keys.onboardingSeen] ?: false, p[Keys.themeMode] ?: "system")
    }
    suspend fun finish(score: Int, earned: Int, tile: Int) = context.dataStore.edit { p ->
        p[Keys.highScore] = maxOf(p[Keys.highScore] ?: 0, score)
        p[Keys.coins] = (p[Keys.coins] ?: 0) + earned
        p[Keys.games] = (p[Keys.games] ?: 0) + 1
        p[Keys.highestTile] = maxOf(p[Keys.highestTile] ?: 0, tile)
    }
    suspend fun completeLevel(level: Level) = context.dataStore.edit { p ->
        p[Keys.unlockedLevel] = maxOf(p[Keys.unlockedLevel] ?: 1, (level.number + 1).coerceAtMost(campaignLevels.size))
        p[Keys.coins] = (p[Keys.coins] ?: 0) + level.reward
    }
    suspend fun completeOnboarding() = context.dataStore.edit { it[Keys.onboardingSeen] = true }
    suspend fun setTheme(mode: String) = context.dataStore.edit { it[Keys.themeMode] = mode }
}
