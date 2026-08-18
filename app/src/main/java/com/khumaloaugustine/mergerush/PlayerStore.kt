package com.khumaloaugustine.mergerush

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("player_progress")

data class PlayerData(
    val highScore: Int = 0, val coins: Int = 0, val games: Int = 0,
    val highestTile: Int = 0, val unlockedLevel: Int = 1,
    val onboardingSeen: Boolean = false, val themeMode: String = "system",
    val dailyStreak: Int = 0, val bestDailyStreak: Int = 0, val lastDailyDay: Int = -1
)

data class SavedSession(val game: GameState, val undos: Int, val shuffles: Int)

class PlayerStore(private val context: Context) {
    private object Keys {
        val highScore = intPreferencesKey("high_score")
        val coins = intPreferencesKey("coins")
        val games = intPreferencesKey("games")
        val highestTile = intPreferencesKey("highest_tile")
        val unlockedLevel = intPreferencesKey("unlocked_level")
        val onboardingSeen = booleanPreferencesKey("onboarding_seen")
        val themeMode = stringPreferencesKey("theme_mode")
        val sessionBoard = stringPreferencesKey("session_board")
        val sessionValues = stringPreferencesKey("session_values")
        val dailyStreak = intPreferencesKey("daily_streak")
        val bestDailyStreak = intPreferencesKey("best_daily_streak")
        val lastDailyDay = intPreferencesKey("last_daily_day")
    }
    val data: Flow<PlayerData> = context.dataStore.data.map { p ->
        PlayerData(
            highScore = p[Keys.highScore] ?: 0, coins = p[Keys.coins] ?: 0,
            games = p[Keys.games] ?: 0, highestTile = p[Keys.highestTile] ?: 0,
            unlockedLevel = p[Keys.unlockedLevel] ?: 1,
            onboardingSeen = p[Keys.onboardingSeen] ?: false,
            themeMode = p[Keys.themeMode] ?: "system",
            dailyStreak = p[Keys.dailyStreak] ?: 0,
            bestDailyStreak = p[Keys.bestDailyStreak] ?: 0,
            lastDailyDay = p[Keys.lastDailyDay] ?: -1
        )
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

    suspend fun completeDaily(day: Int, reward: Int) = context.dataStore.edit { p ->
        if (p[Keys.lastDailyDay] == day) return@edit
        val streak = if (p[Keys.lastDailyDay] == day - 1) (p[Keys.dailyStreak] ?: 0) + 1 else 1
        p[Keys.dailyStreak] = streak
        p[Keys.bestDailyStreak] = maxOf(p[Keys.bestDailyStreak] ?: 0, streak)
        p[Keys.lastDailyDay] = day
        p[Keys.coins] = (p[Keys.coins] ?: 0) + reward
    }

    val session: Flow<SavedSession?> = context.dataStore.data.map { p ->
        val board = p[Keys.sessionBoard]?.split(',')?.mapNotNull(String::toIntOrNull)
        val values = p[Keys.sessionValues]?.split(',')?.mapNotNull(String::toIntOrNull)
        if (board?.size != BOARD_SIZE * BOARD_SIZE || values == null || values.size !in 18..20) null else {
            SavedSession(
                GameState(
                    board = board,
                    score = values[0], nextTile = values[1], gameOver = values[2] == 1,
                    continued = values[3] == 1, combo = values[4], lastGain = values[5],
                    moves = values[6], target = values[7], milestone = values[8].takeIf { it >= 0 },
                    levelNumber = values[9], moveLimit = values[10], won = values[11] == 1,
                    scoreGoal = values[12], comboGoal = values[13], bestCombo = values[14],
                    targetReached = values[15] == 1,
                    fusionEnergy = values.getOrElse(18) { 0 }, fusionValue = values.getOrElse(19) { 0 }
                ),
                undos = values[16], shuffles = values[17]
            )
        }
    }

    suspend fun saveSession(game: GameState, undos: Int, shuffles: Int) = context.dataStore.edit { p ->
        p[Keys.sessionBoard] = game.board.joinToString(",")
        p[Keys.sessionValues] = listOf(
            game.score, game.nextTile, game.gameOver.flag(), game.continued.flag(), game.combo,
            game.lastGain, game.moves, game.target, game.milestone ?: -1, game.levelNumber,
            game.moveLimit, game.won.flag(), game.scoreGoal, game.comboGoal, game.bestCombo,
            game.targetReached.flag(), undos, shuffles, game.fusionEnergy, game.fusionValue
        ).joinToString(",")
    }

    suspend fun clearSession() = context.dataStore.edit {
        it.remove(Keys.sessionBoard)
        it.remove(Keys.sessionValues)
    }

    private fun Boolean.flag() = if (this) 1 else 0
}
