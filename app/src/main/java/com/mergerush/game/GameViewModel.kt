package com.mergerush.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    fun move(direction: Direction) {
        _hint.value = null
        val before = _game.value
        val result = GameEngine.move(before, direction)
        if (result.moved) previous = before
        _game.value = result.state
        if (result.state.gameOver && !saved) saveResult(1)
        if (result.state.won && !saved) {
            campaignLevels.firstOrNull { it.number == result.state.levelNumber }?.let { level -> viewModelScope.launch { store.completeLevel(level) } }
            saved = true
        }
    }
    fun restart() { _game.value = GameEngine.newGame(); previous = null; _undos.value = 3; _shuffles.value = 1; saved = false }
    fun startLevel(level: Level) { _game.value = GameEngine.newLevel(level); previous = null; _undos.value = 3; _shuffles.value = 1; saved = false }
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
    }
    fun completeOnboarding() { viewModelScope.launch { store.completeOnboarding() } }
    fun setTheme(mode: String) { viewModelScope.launch { store.setTheme(mode) } }
    fun replay() {
        val number = _game.value.levelNumber
        if (number == 0) restart() else campaignLevels.first { it.number == number }.let(::startLevel)
    }
    fun undo() {
        val snapshot = previous ?: return
        if (_undos.value == 0 || _game.value.gameOver) return
        _game.value = snapshot.copy(milestone = null)
        previous = null
        _undos.value--
    }
    fun continueAfterReward() { _game.value = GameEngine.continueGame(_game.value) }
    fun doubleCoins() { if (saved) saveResult(1) }
    private fun saveResult(multiplier: Int) {
        val state = _game.value
        val coins = maxOf(1, state.score / 100) * multiplier
        viewModelScope.launch { store.finish(state.score, coins, state.board.maxOrNull() ?: 0) }
        saved = true
    }
}
