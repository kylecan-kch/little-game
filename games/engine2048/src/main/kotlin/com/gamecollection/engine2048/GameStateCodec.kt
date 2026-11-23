package com.gamecollection.engine2048

object GameStateCodec {
    fun encode(state: GameState): String = buildString {
        appendLine("v1")
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
        if (lines.size < 5 || lines[0] != "v1") return null
        val boardRead = readBoard(lines, 1) ?: return null
        val markerIndex = boardRead.nextLine
        if (markerIndex !in lines.indices) return null
        return when (lines[markerIndex]) {
            "-" -> if (markerIndex == lines.lastIndex) GameState(boardRead.board) else null
            "U" -> {
                val undoRead = readBoard(lines, markerIndex + 1) ?: return null
                if (undoRead.nextLine != lines.size) null else GameState(boardRead.board, undoRead.board)
            }
            else -> null
        }
    }

    private data class BoardRead(val board: Board, val nextLine: Int)

    private fun StringBuilder.appendBoard(board: Board) {
        appendLine(board.score)
        appendLine(board.status.name)
        appendLine(board.nextId)
        appendLine(
            board.cells.joinToString(",") { tile ->
                if (tile == null) "." else "${tile.id}:${tile.value}"
            },
        )
    }

    private fun readBoard(lines: List<String>, start: Int): BoardRead? {
        if (start + 3 >= lines.size) return null
        val score = lines[start].toIntOrNull() ?: return null
        if (score < 0) return null
        val status = runCatching { Status.valueOf(lines[start + 1]) }.getOrNull() ?: return null
        val nextId = lines[start + 2].toIntOrNull() ?: return null
        if (nextId <= 0) return null
        val tokens = lines[start + 3].split(",")
        if (tokens.size != 16) return null
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
            board = Board(cells = cells, score = score, status = status, nextId = nextId),
            nextLine = start + 4,
        )
    }

    private fun isTileValue(value: Int): Boolean = value >= 2 && value and (value - 1) == 0
}
