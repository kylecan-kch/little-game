package com.gamecollection.engine2048

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Game2048Test {
    @Test
    fun newGame_placesTwoTilesOfTwoOrFour() {
        val rng = SequenceIntSource(0, 1, 0, 0)
        val state = Game2048(rng).newGame()
        val tiles = state.board.cells.filterNotNull()
        assertEquals(2, tiles.size)
        assertEquals(2, state.board.tileAt(0, 0)!!.value)
        assertEquals(4, state.board.tileAt(0, 1)!!.value)
        assertEquals(0, state.board.score)
        assertEquals(Status.Playing, state.board.status)
        assertFalse(state.canUndo)
    }

    @Test
    fun spawnDistribution_isRoughlyNinetyPercentTwos() {
        val game = Game2048(RandomIntSource(kotlin.random.Random(42)))
        var twos = 0
        var fours = 0
        repeat(400) {
            game.newGame().board.cells.filterNotNull().forEach { tile ->
                when (tile.value) {
                    2 -> twos++
                    4 -> fours++
                    else -> error("Unexpected spawn ${tile.value}")
                }
            }
        }
        assertEquals(800, twos + fours)
        assertTrue("fours=$fours twos=$twos", fours in 40..140)
        assertTrue(twos > fours)
    }

    @Test
    fun left_fourTwos_mergeIntoTwoFours_withoutDoubleMerge() {
        val outcome = move(
            """
            2 2 2 2
            _ _ _ _
            _ _ _ _
            _ _ _ _
            """.trimIndent(),
            Direction.Left,
        )
        assertTrue(outcome.moved)
        assertEquals(listOf(4, 4, 0, 0), outcome.state.board.row(0))
        assertEquals(8, outcome.state.board.score)
        assertEquals(2, outcome.slides.count { it.consumed })
    }

    @Test
    fun left_twoTwoFour_doesNotChainTheMergedFour() {
        val outcome = move(
            """
            2 2 4 _
            _ _ _ _
            _ _ _ _
            _ _ _ _
            """.trimIndent(),
            Direction.Left,
        )
        assertEquals(listOf(4, 4, 0, 0), outcome.state.board.row(0))
        assertEquals(4, outcome.state.board.score)
    }

    @Test
    fun left_fourTwoTwoTwo_keepsTheOriginalFour() {
        val outcome = move(
            """
            4 2 2 2
            _ _ _ _
            _ _ _ _
            _ _ _ _
            """.trimIndent(),
            Direction.Left,
        )
        assertEquals(listOf(4, 4, 2, 0), outcome.state.board.row(0))
        assertEquals(4, outcome.state.board.score)
    }

    @Test
    fun left_fourFourEight_doesNotChainMerge() {
        val outcome = move(
            """
            4 4 8 _
            _ _ _ _
            _ _ _ _
            _ _ _ _
            """.trimIndent(),
            Direction.Left,
        )
        assertEquals(listOf(8, 8, 0, 0), outcome.state.board.row(0))
        assertEquals(8, outcome.state.board.score)
    }

    @Test
    fun left_gapsSlideAndThenMergeOnce() {
        val start = board(
            """
            2 _ _ 2
            _ 2 2 _
            4 _ 2 2
            _ _ _ _
            """.trimIndent(),
        )
        val survivor = start.tileAt(0, 0)!!.id
        val outcome = Game2048(SequenceIntSource()).move(GameState(start), Direction.Left, spawnAfterMove = false)
        assertEquals(listOf(4, 0, 0, 0), outcome.state.board.row(0))
        assertEquals(listOf(4, 0, 0, 0), outcome.state.board.row(1))
        assertEquals(listOf(4, 4, 0, 0), outcome.state.board.row(2))
        assertEquals(survivor, outcome.state.board.tileAt(0, 0)!!.id)
        assertTrue(outcome.slides.filter { it.consumed }.none { it.id == survivor })
        assertEquals(4 + 4 + 4, outcome.state.board.score)
    }

    @Test
    fun right_fourTwos_packTowardTheRight() {
        val outcome = move(
            """
            2 2 2 2
            _ _ _ _
            _ _ _ _
            _ _ _ _
            """.trimIndent(),
            Direction.Right,
        )
        assertEquals(listOf(0, 0, 4, 4), outcome.state.board.row(0))
    }

    @Test
    fun up_columnOfTwos_mergesTowardTheTop() {
        val outcome = move(
            """
            2 _ _ _
            2 _ _ _
            2 _ _ _
            2 _ _ _
            """.trimIndent(),
            Direction.Up,
        )
        assertEquals(listOf(4, 4, 0, 0), outcome.state.board.column(0))
        assertEquals(8, outcome.state.board.score)
    }

    @Test
    fun down_columnOfTwos_mergesTowardTheBottom() {
        val outcome = move(
            """
            2 _ _ _
            2 _ _ _
            2 _ _ _
            2 _ _ _
            """.trimIndent(),
            Direction.Down,
        )
        assertEquals(listOf(0, 0, 4, 4), outcome.state.board.column(0))
    }

    @Test
    fun horizontalNeighbors_doNotMergeWhenSlidingUp() {
        val outcome = move(
            """
            2 2 _ _
            _ _ _ _
            _ _ _ _
            _ _ _ _
            """.trimIndent(),
            Direction.Up,
        )
        assertFalse(outcome.moved)
        assertEquals(listOf(2, 2, 0, 0), outcome.state.board.row(0))
        assertEquals(0, outcome.state.board.score)
    }

    @Test
    fun invalidMove_doesNotSpawnOrSpendRandomness() {
        val game = Game2048(SequenceIntSource())
        val start = GameState(
            board(
                """
                2 4 8 16
                _ _ _ _
                _ _ _ _
                _ _ _ _
                """.trimIndent(),
            ),
        )
        val outcome = game.move(start, Direction.Left)
        assertFalse(outcome.moved)
        assertNull(outcome.spawned)
        assertEquals(start, outcome.state)
    }

    @Test
    fun validMove_spawnsOneTileInACellThatWasEmpty() {
        val start = board(
            """
            2 2 _ _
            _ _ _ _
            _ _ _ _
            _ _ _ _
            """.trimIndent(),
        )
        val outcome = Game2048(SequenceIntSource(0, 1)).move(GameState(start), Direction.Left)
        assertTrue(outcome.moved)
        val spawned = outcome.spawned!!
        assertEquals(2, spawned.value)
        assertEquals(spawned.id, outcome.state.board.tileAt(spawned.row, spawned.col)!!.id)
        val beforeEmpty = slidWithoutSpawn(start, Direction.Left)
        assertNull(beforeEmpty.tileAt(spawned.row, spawned.col))
        assertEquals(start.cells.count { it != null }, outcome.state.board.cells.count { it != null })
    }

    @Test
    fun spawnValue_isFourOnlyWhenTheTenthRollIsZero() {
        val start = GameState(
            board(
                """
                2 _ _ _
                _ _ _ _
                _ _ _ _
                _ _ _ _
                """.trimIndent(),
            ),
        )
        val four = Game2048(SequenceIntSource(0, 0)).move(start, Direction.Right)
        assertEquals(4, four.spawned!!.value)
        val two = Game2048(SequenceIntSource(0, 1)).move(start, Direction.Right)
        assertEquals(2, two.spawned!!.value)
    }

    @Test
    fun reaching2048_winsAndBlocksMovesUntilContinue() {
        val start = GameState(
            board(
                """
                1024 1024 _ _
                _ _ _ _
                _ _ _ _
                _ _ _ _
                """.trimIndent(),
            ),
        )
        val game = Game2048(SequenceIntSource())
        val won = game.move(start, Direction.Left, spawnAfterMove = false)
        assertTrue(won.justWon)
        assertEquals(Status.Won, won.state.board.status)
        assertEquals(2048, won.state.board.tileAt(0, 0)!!.value)
        assertEquals(2048, won.state.board.score)

        val ignored = game.move(won.state, Direction.Left, spawnAfterMove = false)
        assertFalse(ignored.moved)
        assertEquals(won.state, ignored.state)

        val continued = game.continueAfterWin(won.state)
        assertEquals(Status.Continuing, continued.board.status)
        assertTrue(continued.canUndo)
    }

    @Test
    fun continuing_canMergePast2048_withoutWinningAgain() {
        val start = GameState(
            board(
                """
                1024 1024 _ _
                1024 1024 _ _
                _ _ _ _
                _ _ _ _
                """.trimIndent(),
            ),
        )
        val game = Game2048(SequenceIntSource())
        val won = game.move(start, Direction.Left, spawnAfterMove = false)
        assertTrue(won.justWon)
        val continued = game.continueAfterWin(won.state)
        val higher = game.move(continued, Direction.Up, spawnAfterMove = false)
        assertTrue(higher.moved)
        assertFalse(higher.justWon)
        assertEquals(Status.Continuing, higher.state.board.status)
        assertEquals(4096, higher.state.board.tileAt(0, 0)!!.value)
    }

    @Test
    fun continueAfterWin_onADeadBoard_isGameOver() {
        val dead = Board(
            cells = listOf(
                2048, 2, 4, 8,
                16, 32, 64, 128,
                256, 512, 2, 4,
                8, 16, 32, 64,
            ).mapIndexed { index, value -> Tile(index + 1, value) },
            status = Status.Won,
            nextId = 17,
        )
        val game = Game2048()
        assertFalse(game.hasMoves(dead))
        val next = game.continueAfterWin(GameState(dead))
        assertEquals(Status.Lost, next.board.status)
    }

    @Test
    fun checkerboard_hasNoMoves() {
        val full = board(
            """
            2 4 2 4
            4 2 4 2
            2 4 2 4
            4 2 4 2
            """.trimIndent(),
        )
        assertFalse(Game2048().hasMoves(full))
    }

    @Test
    fun fillingTheLastCellWithoutAWin_endsTheGame() {
        val outcome = Game2048(SequenceIntSource(0, 0)).move(
            GameState(
                board(
                    """
                    2 4 8 16
                    4 2 16 8
                    8 16 4 2
                    32 _ 64 128
                    """.trimIndent(),
                ),
            ),
            Direction.Left,
        )
        assertTrue(outcome.moved)
        assertEquals(4, outcome.spawned!!.value)
        assertEquals(Status.Lost, outcome.state.board.status)
        assertFalse(outcome.justWon)
        assertTrue(outcome.state.board.cells.none { it == null })
    }

    @Test
    fun spawnThatMatchesANeighbor_doesNotEndTheGame() {
        val outcome = Game2048(SequenceIntSource(0, 1)).move(
            GameState(
                board(
                    """
                    2 4 8 16
                    4 2 16 8
                    8 16 4 2
                    32 _ 64 128
                    """.trimIndent(),
                ),
            ),
            Direction.Left,
        )
        assertEquals(2, outcome.spawned!!.value)
        assertEquals(Status.Playing, outcome.state.board.status)
        assertTrue(Game2048().hasMoves(outcome.state.board))
    }

    @Test
    fun winningMoveThatAlsoFillsTheBoard_staysAWin() {
        val outcome = Game2048(SequenceIntSource(0, 1)).move(
            GameState(
                board(
                    """
                    1024 1024 2 4
                    8 4 16 8
                    32 16 64 32
                    128 64 256 128
                    """.trimIndent(),
                ),
            ),
            Direction.Left,
        )
        assertTrue(outcome.justWon)
        assertEquals(Status.Won, outcome.state.board.status)
        assertEquals(2048, outcome.state.board.tileAt(0, 0)!!.value)
        assertFalse(Game2048().hasMoves(outcome.state.board))
        val after = Game2048().continueAfterWin(outcome.state)
        assertEquals(Status.Lost, after.board.status)
    }

    @Test
    fun undo_restoresThePreviousBoardOnce() {
        val start = GameState(
            board(
                """
                2 2 _ _
                _ _ _ _
                _ _ _ _
                _ _ _ _
                """.trimIndent(),
            ),
        )
        val game = Game2048(SequenceIntSource(3, 0))
        val moved = game.move(start, Direction.Left)
        assertTrue(moved.state.canUndo)
        assertNotEquals(start.board, moved.state.board)
        val undone = game.undo(moved.state)
        assertEquals(start.board, undone.board)
        assertFalse(undone.canUndo)
        assertEquals(undone, game.undo(undone))
    }

    @Test
    fun invalidMove_doesNotConsumeUndo() {
        val start = GameState(
            board(
                """
                2 2 _ _
                _ _ _ _
                _ _ _ _
                _ _ _ _
                """.trimIndent(),
            ),
        )
        val game = Game2048(SequenceIntSource())
        val moved = game.move(start, Direction.Left, spawnAfterMove = false)
        val ignored = game.move(moved.state, Direction.Left, spawnAfterMove = false)
        assertFalse(ignored.moved)
        assertTrue(ignored.state.canUndo)
        assertEquals(start.board, game.undo(ignored.state).board)
    }

    @Test
    fun continueWhenNotWon_isIgnored() {
        val state = GameState(board("2 _ _ _\n_ _ _ _\n_ _ _ _\n_ _ _ _"))
        assertEquals(state, Game2048().continueAfterWin(state))
    }

    private fun move(grid: String, direction: Direction): MoveOutcome {
        return Game2048(SequenceIntSource()).move(GameState(board(grid)), direction, spawnAfterMove = false)
    }

    private fun slidWithoutSpawn(start: Board, direction: Direction): Board {
        return Game2048(SequenceIntSource()).move(GameState(start), direction, spawnAfterMove = false).state.board
    }
}

class GameStateCodecTest {
    @Test
    fun roundTrip_keepsBoardUndoAndScore() {
        val start = board(
            """
            2 2 _ _
            4 _ 8 _
            _ _ _ _
            _ _ _ 16
            """.trimIndent(),
            score = 12,
        )
        val moved = Game2048(SequenceIntSource(1, 0)).move(GameState(start), Direction.Left)
        val encoded = GameStateCodec.encode(moved.state)
        val decoded = GameStateCodec.decode(encoded)
        assertEquals(moved.state, decoded)
        assertEquals(4, decoded!!.board.tileAt(0, 0)!!.value)
        assertEquals(start, decoded.undo)
    }

    @Test
    fun rejectsGarbage() {
        assertNull(GameStateCodec.decode(""))
        assertNull(GameStateCodec.decode("nope"))
        assertNull(GameStateCodec.decode("v1\n-1\nPlaying\n1\n.\n-"))
        assertNull(GameStateCodec.decode("v1\n0\nPlaying\n1\n.\n-"))
        assertNull(GameStateCodec.decode("v1\n0\nSleeping\n1\n${dots(16)}\n-"))
        assertNull(GameStateCodec.decode("v1\n0\nPlaying\n1\n${List(16) { "1:3" }.joinToString(",")}\n-"))
        assertNull(GameStateCodec.decode("v1\n0\nPlaying\n1\n${List(16) { "1:2" }.joinToString(",")}\n-"))
    }

    private fun dots(count: Int) = List(count) { "." }.joinToString(",")
}

private class SequenceIntSource(vararg values: Int) : IntSource {
    private val queue = ArrayDeque(values.toList())

    override fun nextInt(until: Int): Int {
        check(queue.isNotEmpty()) { "Random source exhausted for bound $until" }
        val value = queue.removeFirst()
        check(value in 0 until until) { "Scripted $value is outside 0 until $until" }
        return value
    }
}

private fun board(grid: String, score: Int = 0, status: Status = Status.Playing): Board {
    val tokens = grid.trim().lines().flatMap { it.trim().split(Regex("\\s+")) }
    require(tokens.size == 16) { "Expected 16 cells, got $tokens" }
    var nextId = 1
    val cells = tokens.map { token ->
        if (token == "_" || token == "." || token == "0") {
            null
        } else {
            Tile(id = nextId++, value = token.toInt())
        }
    }
    return Board(cells = cells, score = score, status = status, nextId = nextId)
}

private fun Board.row(index: Int): List<Int> = List(size) { col -> tileAt(index, col)?.value ?: 0 }

private fun Board.column(index: Int): List<Int> = List(size) { row -> tileAt(row, index)?.value ?: 0 }
