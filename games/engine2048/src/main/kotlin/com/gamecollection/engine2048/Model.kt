package com.gamecollection.engine2048

enum class Direction {
    Left,
    Right,
    Up,
    Down,
}

enum class Status {
    Playing,
    Won,
    Continuing,
    Lost,
}

data class Tile(
    val id: Int,
    val value: Int,
)

data class Board(
    val size: Int = 4,
    val cells: List<Tile?>,
    val score: Int = 0,
    val status: Status = Status.Playing,
    val nextId: Int = 1,
) {
    init {
        require(cells.size == size * size) { "Expected ${size * size} cells, got ${cells.size}" }
    }

    fun tileAt(row: Int, col: Int): Tile? = cells[row * size + col]

    companion object {
        fun empty(size: Int = 4): Board = Board(
            size = size,
            cells = List(size * size) { null },
            nextId = 1,
        )
    }
}

data class GameState(
    val board: Board,
    val undo: Board? = null,
) {
    val canUndo: Boolean get() = undo != null
}

data class Slide(
    val id: Int,
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val fromValue: Int,
    val toValue: Int,
    val consumed: Boolean,
)

data class Spawn(
    val id: Int,
    val value: Int,
    val row: Int,
    val col: Int,
)

data class MoveOutcome(
    val state: GameState,
    val moved: Boolean,
    val slides: List<Slide> = emptyList(),
    val spawned: Spawn? = null,
    val justWon: Boolean = false,
)
