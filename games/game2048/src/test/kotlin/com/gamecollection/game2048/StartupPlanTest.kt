package com.gamecollection.game2048

import com.gamecollection.engine2048.Board
import com.gamecollection.engine2048.Game2048
import com.gamecollection.engine2048.GameState
import com.gamecollection.engine2048.Status
import com.gamecollection.engine2048.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupPlanTest {
    private val themes = setOf("ink", "pixel")
    private val engine = Game2048()

    @Test
    fun readFailure_doesNotRestoreOrPersist() {
        val saved = GameState(Board.empty())
        val plan = planStartup(
            readFailed = true,
            savedState = saved,
            savedBest = 80,
            savedThemeId = "ink",
            themeIds = themes,
            defaultThemeId = "ink",
            normalize = engine::normalize,
        )
        assertTrue(plan.restored == null)
        assertFalse(plan.persist)
        assertEquals(0, plan.best)
        assertEquals("ink", plan.themeId)
    }

    @Test
    fun missingState_startsANewGameAndKeepsBest() {
        val plan = planStartup(
            readFailed = false,
            savedState = null,
            savedBest = 40,
            savedThemeId = "nope",
            themeIds = themes,
            defaultThemeId = "ink",
            normalize = engine::normalize,
        )
        assertNull(plan.restored)
        assertTrue(plan.persist)
        assertEquals(40, plan.best)
        assertEquals("ink", plan.themeId)
    }

    @Test
    fun healthySave_isRestoredWithoutPersisting() {
        val saved = GameState(
            Board(
                cells = listOf(Tile(1, 2), null, null, null) + List(12) { null },
                score = 4,
                status = Status.Playing,
                nextId = 2,
            ),
        )
        val plan = planStartup(
            readFailed = false,
            savedState = saved,
            savedBest = 10,
            savedThemeId = "pixel",
            themeIds = themes,
            defaultThemeId = "ink",
            normalize = engine::normalize,
        )
        assertEquals(saved, plan.restored)
        assertFalse(plan.persist)
        assertEquals(10, plan.best)
        assertEquals("pixel", plan.themeId)
    }

    @Test
    fun stuckPlayingBoard_isNormalizedAndMarkedForPersist() {
        val stuck = GameState(
            Board(
                cells = listOf(2, 4, 2, 4, 4, 2, 4, 2, 2, 4, 2, 4, 4, 2, 4, 2)
                    .mapIndexed { index, value -> Tile(index + 1, value) },
                status = Status.Playing,
                score = 12,
                nextId = 17,
            ),
        )
        val plan = planStartup(
            readFailed = false,
            savedState = stuck,
            savedBest = 12,
            savedThemeId = "ink",
            themeIds = themes,
            defaultThemeId = "ink",
            normalize = engine::normalize,
        )
        assertEquals(Status.Lost, plan.restored!!.board.status)
        assertEquals(stuck.board.cells, plan.restored.board.cells)
        assertTrue(plan.persist)
        assertEquals(12, plan.best)
    }

    @Test
    fun nonFourBoard_isTreatedAsNoSave() {
        val saved = GameState(Board(size = 3, cells = List(9) { null }, nextId = 1))
        val plan = planStartup(
            readFailed = false,
            savedState = saved,
            savedBest = 15,
            savedThemeId = "pixel",
            themeIds = themes,
            defaultThemeId = "ink",
            normalize = engine::normalize,
        )
        assertFalse(isFourByFourSave(saved))
        assertNull(plan.restored)
        assertTrue(plan.persist)
        assertEquals(15, plan.best)
        assertEquals("pixel", plan.themeId)
    }

    @Test
    fun undoBoardOfADifferentSize_isTreatedAsNoSave() {
        val current = Board(size = 4, cells = List(16) { null }, nextId = 1)
        val undo = Board(size = 3, cells = List(9) { null }, nextId = 1)
        val saved = GameState(current, undo)
        val plan = planStartup(
            readFailed = false,
            savedState = saved,
            savedBest = 9,
            savedThemeId = "ink",
            themeIds = themes,
            defaultThemeId = "ink",
            normalize = engine::normalize,
        )
        assertFalse(isFourByFourSave(saved))
        assertNull(plan.restored)
        assertTrue(plan.persist)
        assertEquals(9, plan.best)
    }

    @Test
    fun fourByFourUndo_isRestored() {
        val current = Board(
            cells = listOf(Tile(1, 2)) + List(15) { null },
            nextId = 2,
        )
        val undo = Board(size = 4, cells = List(16) { null }, nextId = 1)
        val saved = GameState(current, undo)
        assertTrue(isFourByFourSave(saved))
        val plan = planStartup(
            readFailed = false,
            savedState = saved,
            savedBest = 0,
            savedThemeId = "ink",
            themeIds = themes,
            defaultThemeId = "ink",
            normalize = engine::normalize,
        )
        assertEquals(saved, plan.restored)
        assertFalse(plan.persist)
    }

    @Test
    fun scoreAboveBest_raisesBestAndPersists() {
        val saved = GameState(
            Board(
                cells = listOf(Tile(1, 2)) + List(15) { null },
                score = 30,
                nextId = 2,
            ),
        )
        val plan = planStartup(
            readFailed = false,
            savedState = saved,
            savedBest = 5,
            savedThemeId = "ink",
            themeIds = themes,
            defaultThemeId = "ink",
            normalize = engine::normalize,
        )
        assertEquals(saved, plan.restored)
        assertEquals(30, plan.best)
        assertTrue(plan.persist)
    }
}
