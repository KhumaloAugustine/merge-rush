package com.khumaloaugustine.mergerush

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random

const val DAILY_LEVEL_NUMBER = 1000

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val store = PlayerStore(application)
    private val _game = MutableStateFlow(GameEngine.newGame())
    val game = _game.asStateFlow()
    val player = store.data.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerData(onboardingSeen = true))
    private var saved = false
    private var previous: GameState? = null
    private val _undos = MutableStateFlow(3)
    val undos = _undos.asStateFlow()
    private val _hint = MutableStateFlow<Direction?>(null)
    val hint = _hint.asStateFlow()
    private val _shuffles = MutableStateFlow(1)
    val shuffles = _shuffles.asStateFlow()

    init {
        viewModelScope.launch {
            store.session.first()?.let {
                _game.value = it.game
                _undos.value = it.undos
                _shuffles.value = it.shuffles
                saved = it.game.won || it.game.gameOver
            }
        }
    }

    private fun persistSession() {
        val state = _game.value
        val undosLeft = _undos.value
        val shufflesLeft = _shuffles.value
        viewModelScope.launch { store.saveSession(state, undosLeft, shufflesLeft) }
    }

    fun move(direction: Direction) {
        _hint.value = null
        val before = _game.value
        val result = GameEngine.move(before, direction)
        if (result.moved) previous = before
        _game.value = result.state
        persistSession()
        if (result.state.gameOver && !saved) saveResult(1)
        if (result.state.won && !saved) {
            if (result.state.levelNumber == DAILY_LEVEL_NUMBER) {
                val day = LocalDate.now().toEpochDay().toInt()
                viewModelScope.launch { store.completeDaily(day, 500 + player.value.dailyStreak * 50) }
            } else campaignLevels.firstOrNull { it.number == result.state.levelNumber }?.let { level -> viewModelScope.launch { store.completeLevel(level) } }
            saved = true
        }
    }
    fun restart() { _game.value = GameEngine.newGame(); previous = null; _undos.value = 3; _shuffles.value = 1; saved = false; persistSession() }
    fun startLevel(level: Level) { _game.value = GameEngine.newLevel(level); previous = null; _undos.value = 3; _shuffles.value = 1; saved = false; persistSession() }
    fun startDailyChallenge() {
        val day = LocalDate.now().toEpochDay().toInt()
        val type = day.mod(3)
        val level = when (type) {
            0 -> Level(DAILY_LEVEL_NUMBER, "Daily Speed Fusion", 128, 65, 500, comboGoal = 3)
            1 -> Level(DAILY_LEVEL_NUMBER, "Daily Score Fusion", 256, 100, 500, scoreGoal = 3_500)
            else -> Level(DAILY_LEVEL_NUMBER, "Daily Master Fusion", 256, 115, 500, scoreGoal = 3_000, comboGoal = 4)
        }
        _game.value = GameEngine.newLevel(level, Random(day))
        previous = null; _undos.value = 3; _shuffles.value = 1; saved = false; persistSession()
    }
    fun nextLevel() {
        val next = campaignLevels.firstOrNull { it.number == _game.value.levelNumber + 1 }
        if (next != null) startLevel(next)
    }
    fun showHint() { _hint.value = GameEngine.suggestMove(_game.value) }
    fun shuffle() {
        if (_shuffles.value == 0 || _game.value.won) return
        previous = _game.value
        _game.value = GameEngine.shuffle(_game.value)
        _hint.value = null
        _shuffles.value--
        persistSession()
    }
    fun activateFusionPulse() {
        val before = _game.value
        val after = GameEngine.activateFusionPulse(before)
        if (after == before) return
        previous = before
        _game.value = after
        _hint.value = null
        persistSession()
        if (after.won && !saved) {
            if (after.levelNumber == DAILY_LEVEL_NUMBER) {
                val day = LocalDate.now().toEpochDay().toInt()
                viewModelScope.launch { store.completeDaily(day, 500 + player.value.dailyStreak * 50) }
            } else campaignLevels.firstOrNull { it.number == after.levelNumber }?.let { level -> viewModelScope.launch { store.completeLevel(level) } }
            saved = true
        }
    }
    fun completeOnboarding() { viewModelScope.launch { store.completeOnboarding() } }
    fun setTheme(mode: String) { viewModelScope.launch { store.setTheme(mode) } }
    fun replay() {
        val number = _game.value.levelNumber
        if (number == 0) restart() else if (number == DAILY_LEVEL_NUMBER) startDailyChallenge() else campaignLevels.first { it.number == number }.let(::startLevel)
    }
    fun undo() {
        val snapshot = previous ?: return
        if (_undos.value == 0 || _game.value.gameOver) return
        _game.value = snapshot.copy(milestone = null)
        previous = null
        _undos.value--
        persistSession()
    }
    fun continueAfterReward() { _game.value = GameEngine.continueGame(_game.value); persistSession() }
    fun doubleCoins() { if (saved) saveResult(1) }
    private fun saveResult(multiplier: Int) {
        val state = _game.value
        val coins = maxOf(1, state.score / 100) * multiplier
        viewModelScope.launch { store.finish(state.score, coins, state.board.maxOrNull() ?: 0) }
        saved = true
    }
}
