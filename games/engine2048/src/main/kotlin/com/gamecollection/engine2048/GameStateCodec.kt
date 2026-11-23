package com.gamecollection.engine2048

object GameStateCodec {
    private const val MIN_SIZE = 2
    private const val MAX_SIZE = 8

    fun encode(state: GameState): String = buildString {
        appendLine("v2")
        appendBoard(state.board)
        val undo = state.undo
        if (undo == null) {
            append("-")
        } else {
            appendLine("U")
            appendBoard(undo)
        }
    }.trimEnd()

    fun decode(text: String): GameState? {
        val lines = text.trim().lines()
        if (lines.isEmpty()) return null
        val hasSize = when (lines[0]) {
            "v1" -> false
            "v2" -> true
            else -> return null
        }
        val boardRead = readBoard(lines, 1, hasSize) ?: return null
        val markerIndex = boardRead.nextLine
        if (markerIndex !in lines.indices) return null
        return when (lines[markerIndex]) {
            "-" -> if (markerIndex == lines.lastIndex) GameState(boardRead.board) else null
            "U" -> {
                val undoRead = readBoard(lines, markerIndex + 1, hasSize) ?: return null
                if (undoRead.nextLine != lines.size) null else GameState(boardRead.board, undoRead.board)
            }
            else -> null
        }
    }

    private data class BoardRead(val board: Board, val nextLine: Int)

    private fun StringBuilder.appendBoard(board: Board) {
        appendLine(board.size)
        appendLine(board.score)
        appendLine(board.status.name)
        appendLine(board.nextId)
        appendLine(
            board.cells.joinToString(",") { tile ->
                if (tile == null) "." else "${tile.id}:${tile.value}"
            },
        )
    }

    private fun readBoard(lines: List<String>, start: Int, hasSize: Boolean): BoardRead? {
        var index = start
        val size = if (hasSize) {
            val parsed = lines.getOrNull(index)?.toIntOrNull() ?: return null
            if (parsed !in MIN_SIZE..MAX_SIZE) return null
            index += 1
            parsed
        } else {
            4
        }
        if (index + 3 >= lines.size) return null
        val score = lines[index].toIntOrNull() ?: return null
        if (score < 0) return null
        val status = runCatching { Status.valueOf(lines[index + 1]) }.getOrNull() ?: return null
        val nextId = lines[index + 2].toIntOrNull() ?: return null
        if (nextId <= 0) return null
        val tokens = lines[index + 3].split(",")
        if (tokens.size != size * size) return null
        val seen = HashSet<Int>()
        var maxId = 0
        val cells = tokens.map { token ->
            if (token == ".") {
                null
            } else {
                val parts = token.split(":")
                if (parts.size != 2) return null
                val id = parts[0].toIntOrNull() ?: return null
                val value = parts[1].toIntOrNull() ?: return null
                if (id <= 0 || !seen.add(id) || !isTileValue(value)) return null
                if (id > maxId) maxId = id
                Tile(id, value)
            }
        }
        if (nextId <= maxId) return null
        return BoardRead(
            board = Board(size = size, cells = cells, score = score, status = status, nextId = nextId),
            nextLine = index + 4,
        )
    }

    private fun isTileValue(value: Int): Boolean = value >= 2 && value and (value - 1) == 0
}
