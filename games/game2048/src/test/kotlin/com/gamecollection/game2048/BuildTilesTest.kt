package com.gamecollection.game2048

import com.gamecollection.engine2048.Board
import com.gamecollection.engine2048.Slide
import com.gamecollection.engine2048.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BuildTilesTest {
    @Test
    fun undoWithoutSlides_givesMovedTilesANewMoveId() {
        val previous = listOf(
            tile(id = 1, row = 0, col = 0, moveId = 4),
            tile(id = 2, row = 0, col = 1, moveId = 4),
        )
        val board = boardWith(
            Tile(1, 2) to (1 to 0),
            Tile(2, 4) to (0 to 1),
        )
        val tiles = buildTiles(
            board = board,
            slides = emptyList(),
            spawned = null,
            serial = 9,
            animateSpawns = false,
            previous = previous,
        )
        val moved = tiles.first { it.id == 1 }
        val stayed = tiles.first { it.id == 2 }
        assertEquals(9, moved.moveId)
        assertEquals(1, moved.row)
        assertEquals(0, moved.col)
        assertEquals(1, moved.fromRow)
        assertEquals(0, moved.fromCol)
        assertFalse(moved.spawned)
        assertEquals(4, stayed.moveId)
        assertEquals(0, stayed.row)
        assertEquals(1, stayed.col)
    }

    @Test
    fun slide_recordsTheOriginAndANewMoveId() {
        val previous = listOf(tile(id = 7, row = 0, col = 0, moveId = 1))
        val board = boardWith(Tile(7, 4) to (0 to 1))
        val tiles = buildTiles(
            board = board,
            slides = listOf(
                Slide(
                    id = 7,
                    fromRow = 0,
                    fromCol = 0,
                    toRow = 0,
                    toCol = 1,
                    fromValue = 2,
                    toValue = 4,
                    consumed = false,
                ),
            ),
            spawned = null,
            serial = 2,
            animateSpawns = false,
            previous = previous,
        )
        val tile = tiles.single()
        assertEquals(2, tile.moveId)
        assertEquals(0, tile.fromRow)
        assertEquals(0, tile.fromCol)
        assertEquals(0, tile.row)
        assertEquals(1, tile.col)
        assertEquals(true, tile.merged)
    }

    private fun tile(id: Int, row: Int, col: Int, moveId: Long) = TileVisual(
        id = id,
        value = 2,
        row = row,
        col = col,
        fromRow = row,
        fromCol = col,
        spawned = false,
        merged = false,
        moveId = moveId,
    )

    private fun boardWith(vararg placed: Pair<Tile, Pair<Int, Int>>): Board {
        val cells = MutableList<Tile?>(16) { null }
        placed.forEach { (tile, position) ->
            val (row, col) = position
            cells[row * 4 + col] = tile
        }
        val maxId = placed.maxOf { it.first.id }
        return Board(cells = cells, nextId = maxId + 1)
    }
}
