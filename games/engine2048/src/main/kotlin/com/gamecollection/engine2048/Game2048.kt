package com.gamecollection.engine2048

/**
 * Classic 2048 rules, with no Android or UI dependencies.
 *
 * Tiles slide in [Direction] order and merge once per move. A row such as
 * 2,2,2,2 becomes 4,4 and never 8 in a single swipe. After a move that
 * actually changes the board, one new tile appears: 2 with probability 90%,
 * or 4 with probability 10%.
 */
class Game2048(
    private val rng: IntSource = RandomIntSource(),
) {
    fun newGame(size: Int = 4): GameState {
        require(size >= 2)
        var board = Board.empty(size)
        board = spawnTile(board).board
        board = spawnTile(board).board
        return GameState(board)
    }

    fun move(
        state: GameState,
        direction: Direction,
        spawnAfterMove: Boolean = true,
    ): MoveOutcome {
        val board = state.board
        if (board.status == Status.Won || board.status == Status.Lost) {
            return MoveOutcome(state, moved = false)
        }

        val slid = slide(board, direction)
        if (slid.slides.isEmpty()) {
            return MoveOutcome(state, moved = false)
        }

        var next = board.copy(
            cells = slid.cells,
            score = board.score + slid.scoreGain,
        )
        val spawned = if (spawnAfterMove) {
            val placed = spawnTile(next)
            next = placed.board
            placed.spawn
        } else {
            null
        }

        val createdWin = board.status == Status.Playing &&
            next.cells.any { (it?.value ?: 0) >= WIN_VALUE }
        val over = !hasMoves(next)
        next = when {
            createdWin -> next.copy(status = Status.Won)
            over -> next.copy(status = Status.Lost)
            else -> next.copy(status = board.status)
        }

        return MoveOutcome(
            state = GameState(board = next, undo = board),
            moved = true,
            slides = slid.slides,
            spawned = spawned,
            justWon = createdWin,
        )
    }

    fun undo(state: GameState): GameState {
        val previous = state.undo ?: return state
        return GameState(board = previous, undo = null)
    }

    fun continueAfterWin(state: GameState): GameState {
        if (state.board.status != Status.Won) return state
        val status = if (hasMoves(state.board)) Status.Continuing else Status.Lost
        return state.copy(board = state.board.copy(status = status))
    }

    fun hasMoves(board: Board): Boolean {
        if (board.cells.any { it == null }) return true
        val size = board.size
        for (row in 0 until size) {
            for (col in 0 until size) {
                val value = board.tileAt(row, col)!!.value
                if (col + 1 < size && board.tileAt(row, col + 1)!!.value == value) return true
                if (row + 1 < size && board.tileAt(row + 1, col)!!.value == value) return true
            }
        }
        return false
    }

    private data class Placement(val tile: Tile, val row: Int, val col: Int)

    private data class SlideSeed(
        val tile: Tile,
        val fromRow: Int,
        val fromCol: Int,
        val toIndex: Int,
        val newValue: Int,
        val consumed: Boolean,
    )

    private data class Slid(
        val cells: List<Tile?>,
        val slides: List<Slide>,
        val scoreGain: Int,
    )

    private data class Spawned(val board: Board, val spawn: Spawn)

    private fun slide(board: Board, direction: Direction): Slid {
        val size = board.size
        val nextCells = MutableList<Tile?>(size * size) { null }
        val slides = mutableListOf<Slide>()
        for (line in 0 until size) {
            val coords = lineCoordinates(size, direction, line)
            val placed = coords.mapNotNull { (row, col) ->
                board.tileAt(row, col)?.let { Placement(it, row, col) }
            }
            val (compact, seeds) = collapse(placed)
            coords.forEachIndexed { index, (row, col) ->
                nextCells[row * size + col] = compact.getOrNull(index)
            }
            for (seed in seeds) {
                val (toRow, toCol) = coords[seed.toIndex]
                slides += Slide(
                    id = seed.tile.id,
                    fromRow = seed.fromRow,
                    fromCol = seed.fromCol,
                    toRow = toRow,
                    toCol = toCol,
                    fromValue = seed.tile.value,
                    toValue = seed.newValue,
                    consumed = seed.consumed,
                )
            }
        }
        val meaningful = slides.filter { slide ->
            slide.consumed ||
                slide.fromRow != slide.toRow ||
                slide.fromCol != slide.toCol ||
                slide.fromValue != slide.toValue
        }
        val scoreGain = meaningful
            .filter { !it.consumed && it.toValue != it.fromValue }
            .sumOf { it.toValue }
        return Slid(nextCells.toList(), meaningful, scoreGain)
    }

    private fun collapse(source: List<Placement>): Pair<List<Tile>, List<SlideSeed>> {
        val out = mutableListOf<Tile>()
        val seeds = mutableListOf<SlideSeed>()
        var index = 0
        while (index < source.size) {
            val current = source[index]
            val next = source.getOrNull(index + 1)
            if (next != null && next.tile.value == current.tile.value) {
                val merged = current.tile.value * 2
                val dest = out.size
                out += current.tile.copy(value = merged)
                seeds += SlideSeed(current.tile, current.row, current.col, dest, merged, consumed = false)
                seeds += SlideSeed(next.tile, next.row, next.col, dest, next.tile.value, consumed = true)
                index += 2
            } else {
                val dest = out.size
                out += current.tile
                seeds += SlideSeed(
                    current.tile,
                    current.row,
                    current.col,
                    dest,
                    current.tile.value,
                    consumed = false,
                )
                index += 1
            }
        }
        return out to seeds
    }

    private fun lineCoordinates(size: Int, direction: Direction, line: Int): List<Pair<Int, Int>> =
        when (direction) {
            Direction.Left -> List(size) { col -> line to col }
            Direction.Right -> List(size) { step -> line to (size - 1 - step) }
            Direction.Up -> List(size) { row -> row to line }
            Direction.Down -> List(size) { step -> (size - 1 - step) to line }
        }

    private fun spawnTile(board: Board): Spawned {
        val empties = board.cells.indices.filter { board.cells[it] == null }
        require(empties.isNotEmpty()) { "Cannot spawn without an empty cell" }
        val slot = empties[rng.nextInt(empties.size)]
        val value = if (rng.nextInt(10) == 0) 4 else 2
        val tile = Tile(id = board.nextId, value = value)
        val cells = board.cells.toMutableList()
        cells[slot] = tile
        return Spawned(
            board = board.copy(cells = cells.toList(), nextId = board.nextId + 1),
            spawn = Spawn(
                id = tile.id,
                value = tile.value,
                row = slot / board.size,
                col = slot % board.size,
            ),
        )
    }

    private companion object {
        const val WIN_VALUE = 2048
    }
}
