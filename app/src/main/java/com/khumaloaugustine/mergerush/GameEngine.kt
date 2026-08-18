package com.khumaloaugustine.mergerush

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
    val targetReached: Boolean = false,
    val fusionEnergy: Int = 0,
    val fusionValue: Int = 0
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
            targetReached = if (state.levelNumber == 0) false else targetReached,
            fusionEnergy = if (rawGain > 0) (state.fusionEnergy + 1).coerceAtMost(3) else state.fusionEnergy,
            fusionValue = 0
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

    fun activateFusionPulse(state: GameState): GameState {
        if (state.fusionEnergy < 3 || state.gameOver || state.won) return state
        val pair = state.board.withIndex().filter { it.value > 0 }
            .groupBy { it.value }.filterValues { it.size >= 2 }.minByOrNull { it.key }?.value
            ?: return state
        val value = pair.first().value
        val board = state.board.toMutableList()
        board[pair[0].index] = value * 2
        board[pair[1].index] = 0
        val reachedNow = !state.targetReached && board.maxOrNull()!! >= state.target
        val targetReached = state.targetReached || reachedNow
        val gain = value * 2 + if (reachedNow) state.target * 2 else 0
        val score = state.score + gain
        val won = state.levelNumber > 0 && targetReached && score >= state.scoreGoal && state.bestCombo >= state.comboGoal
        return state.copy(
            board = board, score = score, lastGain = gain,
            target = if (reachedNow && state.levelNumber == 0) state.target * 2 else state.target,
            milestone = if (reachedNow) state.target else null,
            targetReached = if (state.levelNumber == 0) false else targetReached,
            won = won, fusionEnergy = 0, fusionValue = value * 2
        )
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
        .mapIndexed { index, direction ->
            val first = move(state.copy(gameOver = false, won = false), direction, Random(10_000 + index))
            direction to if (!first.moved) Double.NEGATIVE_INFINITY else {
                val followUp = Direction.entries.mapIndexed { nextIndex, nextDirection ->
                    val second = move(first.state.copy(gameOver = false, won = false), nextDirection, Random(20_000 + index * 10 + nextIndex))
                    if (second.moved) boardQuality(second.state, second.gained) else Double.NEGATIVE_INFINITY
                }.maxOrNull() ?: 0.0
                boardQuality(first.state, first.gained) + followUp * 0.42
            }
        }
        .maxByOrNull { it.second }
        ?.takeIf { it.second.isFinite() }
        ?.first

    private fun boardQuality(state: GameState, gained: Int): Double {
        if (state.won) return 1_000_000.0
        if (state.gameOver) return -1_000_000.0
        val empty = state.board.count { it == 0 }
        val largest = state.board.maxOrNull() ?: 2
        val corners = listOf(0, BOARD_SIZE - 1, state.board.lastIndex - BOARD_SIZE + 1, state.board.lastIndex)
        val cornerBonus = if (corners.any { state.board[it] == largest }) 180 else 0
        val targetProgress = (largest.toDouble() / state.target.coerceAtLeast(2)).coerceAtMost(1.0) * 600
        val scoreProgress = if (state.scoreGoal > 0) (state.score.toDouble() / state.scoreGoal).coerceAtMost(1.0) * 400 else 0.0
        val comboProgress = if (state.comboGoal > 0) (state.bestCombo.toDouble() / state.comboGoal).coerceAtMost(1.0) * 300 else state.combo * 35.0
        return gained * 9.0 + empty * empty * 18.0 + cornerBonus + targetProgress + scoreProgress + comboProgress
    }

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
