package com.mergerush.game

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {
    @Test fun mergesPairOnce() {
        val board = listOf(2, 2, 2, 2) + List(12) { 0 }
        val result = GameEngine.move(GameState(board), Direction.LEFT, Random(1))
        assertEquals(listOf(4, 4), result.state.board.take(2))
        assertEquals(8, result.gained)
    }
    @Test fun fullAlternatingBoardHasNoMoves() {
        val board = List(16) { i -> if ((i / 4 + i % 4) % 2 == 0) 2 else 4 }
        assertFalse(GameEngine.hasMoves(board))
    }
    @Test fun adjacentMatchAllowsMove() {
        val board = List(16) { 2 }.toMutableList().also { it[1] = 4; it[2] = 8; it[3] = 16 }
        assertTrue(GameEngine.hasMoves(board))
    }
    @Test fun consecutiveMergesBuildComboMultiplier() {
        val first = GameEngine.move(GameState(listOf(2, 2, 0, 0) + List(12) { 0 }), Direction.LEFT, Random(2)).state
        val prepared = first.copy(board = listOf(4, 4, 0, 0) + first.board.drop(4))
        val second = GameEngine.move(prepared, Direction.LEFT, Random(3))
        assertEquals(2, second.state.combo)
        assertTrue(second.gained >= 16)
    }
    @Test fun reachingGoalAwardsMilestoneBonus() {
        val result = GameEngine.move(GameState(board = listOf(32, 32, 0, 0) + List(12) { 0 }), Direction.LEFT, Random(4))
        assertEquals(64, result.state.milestone)
        assertEquals(128, result.state.target)
        assertEquals(192, result.gained)
    }
    @Test fun campaignLevelWinsAtItsTarget() {
        val level = Level(1, "Test", 16, 20, 25)
        val state = GameState(board = listOf(8, 8, 0, 0) + List(12) { 0 }, target = level.target, levelNumber = level.number, moveLimit = level.moveLimit)
        val result = GameEngine.move(state, Direction.LEFT, Random(5))
        assertTrue(result.state.won)
        assertFalse(result.state.gameOver)
    }
    @Test fun campaignEndsWhenMoveBudgetIsSpent() {
        val state = GameState(board = listOf(2, 0, 0, 0) + List(12) { 0 }, target = 64, levelNumber = 2, moveLimit = 1)
        val result = GameEngine.move(state, Direction.RIGHT, Random(6))
        assertTrue(result.state.gameOver)
        assertFalse(result.state.won)
    }
    @Test fun hintPrefersAMergingMove() {
        val state = GameState(board = listOf(2, 2, 0, 0) + List(12) { 0 })
        assertTrue(GameEngine.suggestMove(state) in listOf(Direction.LEFT, Direction.RIGHT))
    }
    @Test fun advancedLevelWaitsForComboObjective() {
        val state = GameState(board = listOf(8, 8, 0, 0) + List(12) { 0 }, target = 16, levelNumber = 11, moveLimit = 20, comboGoal = 3)
        val result = GameEngine.move(state, Direction.LEFT, Random(7))
        assertTrue(result.state.targetReached)
        assertFalse(result.state.won)
    }
    @Test fun advancedLevelWinsWhenAllObjectivesAreMet() {
        val state = GameState(board = listOf(8, 8, 0, 0) + List(12) { 0 }, score = 100, target = 16, levelNumber = 11, moveLimit = 20, scoreGoal = 100, comboGoal = 1)
        assertTrue(GameEngine.move(state, Direction.LEFT, Random(8)).state.won)
    }
    @Test fun shufflePreservesEveryTile() {
        val board = listOf(2, 4, 8, 16) + List(12) { 0 }
        assertEquals(board.sorted(), GameEngine.shuffle(GameState(board = board), Random(9)).board.sorted())
    }
    @Test fun campaignHasThreeIncreasingChallengeChapters() {
        assertEquals(40, campaignLevels.size)
        assertTrue(campaignLevels.last().target > campaignLevels.first().target)
        assertEquals(5, campaignLevels.last().comboGoal)
    }
}
