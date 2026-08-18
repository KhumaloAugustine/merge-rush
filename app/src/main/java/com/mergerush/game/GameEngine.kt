package com.mergerush.game

import kotlin.random.Random

const val BOARD_SIZE = 4

data class GameState(
    val board: List<Int> = List(BOARD_SIZE * BOARD_SIZE) { 0 },
    val score: Int = 0,
    val nextTile: Int = 2,
    val gameOver: Boolean = false,
    val continued: Boolean = false,
    val combo: Int = 0,
    val lastGain: Int = 0,
    val moves: Int = 0,
    val target: Int = 64,
    val milestone: Int? = null,
    val levelNumber: Int = 0,
    val moveLimit: Int = 0,
    val won: Boolean = false,
    val scoreGoal: Int = 0,
    val comboGoal: Int = 0,
    val bestCombo: Int = 0,
    val targetReached: Boolean = false
)

data class MoveResult(val state: GameState, val gained: Int, val moved: Boolean)

object GameEngine {
    fun newGame(random: Random = Random.Default): GameState = addRandomTile(addRandomTile(GameState(), random), random)

    fun newLevel(level: Level, random: Random = Random.Default): GameState =
        addRandomTile(addRandomTile(GameState(target = level.target, levelNumber = level.number, moveLimit = level.moveLimit, scoreGoal = level.scoreGoal, comboGoal = level.comboGoal), random), random)

    fun move(state: GameState, direction: Direction, random: Random = Random.Default): MoveResult {
        if (state.gameOver || state.won) return MoveResult(state, 0, false)
        val lines = indices(direction)
        val output = state.board.toMutableList()
        var rawGain = 0
        lines.forEach { line ->
            val values = line.map { state.board[it] }.filter { it != 0 }.toMutableList()
            val merged = mutableListOf<Int>()
            var i = 0
            while (i < values.size) {
                if (i + 1 < values.size && values[i] == values[i + 1]) {
                    val value = values[i] * 2
                    merged += value; rawGain += value; i += 2
                } else { merged += values[i]; i++ }
            }
            while (merged.size < BOARD_SIZE) merged += 0
            line.forEachIndexed { index, boardIndex -> output[boardIndex] = merged[index] }
        }
        if (output == state.board) return MoveResult(state.copy(gameOver = !hasMoves(state.board), combo = 0, lastGain = 0, milestone = null), 0, false)
        val combo = if (rawGain > 0) state.combo + 1 else 0
        val multiplier = combo.coerceIn(1, 5)
        val gained = rawGain * multiplier
        val reachedNow = !state.targetReached && output.maxOrNull()!! >= state.target
        val targetReached = state.targetReached || reachedNow
        val bestCombo = maxOf(state.bestCombo, combo)
        val projectedScore = state.score + gained + if (reachedNow) state.target * 2 else 0
        val campaignWin = state.levelNumber > 0 && targetReached && projectedScore >= state.scoreGoal && bestCombo >= state.comboGoal
        val bonus = if (reachedNow) state.target * 2 else 0
        val totalGain = gained + bonus
        val moved = state.copy(
            board = output,
            score = state.score + totalGain,
            combo = combo,
            lastGain = totalGain,
            moves = state.moves + 1,
            target = if (reachedNow && state.levelNumber == 0) state.target * 2 else state.target,
            milestone = if (reachedNow) state.target else null,
            won = campaignWin,
            bestCombo = bestCombo,
            targetReached = if (state.levelNumber == 0) false else targetReached
        )
        val withTile = addRandomTile(moved, random)
        val outOfMoves = withTile.moveLimit > 0 && withTile.moves >= withTile.moveLimit && !withTile.won
        return MoveResult(withTile.copy(gameOver = outOfMoves || !hasMoves(withTile.board)), totalGain, true)
    }

    fun continueGame(state: GameState): GameState {
        if (!state.gameOver || state.continued) return state
        val crowded = state.board.withIndex().filter { it.value != 0 }.sortedByDescending { it.value }.take(3)
        val board = state.board.toMutableList()
        crowded.forEach { board[it.index] = 0 }
        return state.copy(board = board, gameOver = false, continued = true)
    }

    fun hasMoves(board: List<Int>): Boolean {
        if (board.any { it == 0 }) return true
        for (row in 0 until BOARD_SIZE) for (col in 0 until BOARD_SIZE) {
            val value = board[row * BOARD_SIZE + col]
            if (col + 1 < BOARD_SIZE && value == board[row * BOARD_SIZE + col + 1]) return true
            if (row + 1 < BOARD_SIZE && value == board[(row + 1) * BOARD_SIZE + col]) return true
        }
        return false
    }

    fun suggestMove(state: GameState): Direction? = Direction.entries
        .map { direction -> direction to move(state.copy(gameOver = false, won = false), direction, Random(0)) }
        .filter { it.second.moved }
        .maxWithOrNull(compareBy<Pair<Direction, MoveResult>> { it.second.gained }.thenBy { it.second.state.board.count { tile -> tile == 0 } })
        ?.first

    fun shuffle(state: GameState, random: Random = Random.Default): GameState {
        if (state.won) return state
        var shuffled = state.board.shuffled(random)
        repeat(12) { if (!hasMoves(shuffled)) shuffled = state.board.shuffled(random) }
        return state.copy(board = shuffled, gameOver = !hasMoves(shuffled), combo = 0, lastGain = 0, milestone = null)
    }

    private fun addRandomTile(state: GameState, random: Random): GameState {
        val empty = state.board.indices.filter { state.board[it] == 0 }
        if (empty.isEmpty()) return state
        val board = state.board.toMutableList()
        val value = if (random.nextInt(10) == 0) 4 else 2
        board[empty[random.nextInt(empty.size)]] = value
        return state.copy(board = board, nextTile = if (random.nextInt(10) == 0) 4 else 2)
    }

    private fun indices(direction: Direction): List<List<Int>> = when (direction) {
        Direction.LEFT -> (0 until BOARD_SIZE).map { r -> (0 until BOARD_SIZE).map { c -> r * BOARD_SIZE + c } }
        Direction.RIGHT -> (0 until BOARD_SIZE).map { r -> (BOARD_SIZE - 1 downTo 0).map { c -> r * BOARD_SIZE + c } }
        Direction.UP -> (0 until BOARD_SIZE).map { c -> (0 until BOARD_SIZE).map { r -> r * BOARD_SIZE + c } }
        Direction.DOWN -> (0 until BOARD_SIZE).map { c -> (BOARD_SIZE - 1 downTo 0).map { r -> r * BOARD_SIZE + c } }
    }
}

enum class Direction { UP, DOWN, LEFT, RIGHT }
