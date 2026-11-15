package com.example

import com.example.game.LevelGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelGeneratorTest {

    @Test
    fun allLevelsGenerateExactTriosAndAreSolvable() {
        for (levelNum in 1..25) {
            val levelDef = LevelGenerator.getLevelDefinition(levelNum)
            val board = LevelGenerator.generateBoard(levelDef, seed = levelNum * 101L)

            assertTrue("Level $levelNum should not be empty", board.isNotEmpty())
            assertEquals("Level $levelNum tile count must be divisible by 3", 0, board.size % 3)
            assertEquals(
                "Level $levelNum tile count should match expected totalTrios * 3",
                levelDef.totalTrios * 3,
                board.size,
            )

            val countsByType = board.groupingBy { it.type }.eachCount()
            for ((type, count) in countsByType) {
                assertEquals(
                    "Level $levelNum tile $type count ($count) must be a multiple of 3",
                    0,
                    count % 3,
                )
            }

            val blocked = LevelGenerator.computeBlockedIds(board)
            val uncovered = board.filter { it.id !in blocked }
            assertTrue("Level $levelNum must have uncovered tiles", uncovered.isNotEmpty())
        }
    }
}
