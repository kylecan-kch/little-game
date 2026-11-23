package com.gamecollection.game2048

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gamecollection.engine2048.Board
import com.gamecollection.engine2048.Direction
import com.gamecollection.engine2048.Game2048
import com.gamecollection.engine2048.GameState
import com.gamecollection.engine2048.Slide
import com.gamecollection.engine2048.Spawn
import com.gamecollection.engine2048.Status
import com.gamecollection.game2048.data.Game2048Repository
import com.gamecollection.game2048.theme.ArtStyles
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class TileVisual(
    val id: Int,
    val value: Int,
    val row: Int,
    val col: Int,
    val fromRow: Int,
    val fromCol: Int,
    val spawned: Boolean,
    val merged: Boolean,
    val moveId: Long,
)

data class GhostVisual(
    val id: Int,
    val value: Int,
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val moveId: Long,
)

data class Game2048UiState(
    val loading: Boolean = true,
    val tiles: List<TileVisual> = emptyList(),
    val ghosts: List<GhostVisual> = emptyList(),
    val score: Int = 0,
    val best: Int = 0,
    val canUndo: Boolean = false,
    val status: Status = Status.Playing,
    val themeId: String = ArtStyles.DEFAULT_ID,
    val confirmNewGame: Boolean = false,
    val busy: Boolean = false,
    val moveSerial: Long = 0,
    val swipeSerial: Long = 0,
)

class Game2048ViewModel(
    private val repository: Game2048Repository,
    private val engine: Game2048 = Game2048(),
) : ViewModel() {
    private val _ui = MutableStateFlow(Game2048UiState())
    val ui: StateFlow<Game2048UiState> = _ui.asStateFlow()

    private val persistMutex = Mutex()
    private var gameState = GameState(Board.empty())
    private var best = 0
    private var themeId = ArtStyles.DEFAULT_ID
    private var moveSerial = 0L
    private var swipeSerial = 0L

    init {
        viewModelScope.launch {
            val saved = try {
                repository.load()
            } catch (_: Throwable) {
                null
            }
            best = saved?.best ?: 0
            themeId = saved?.themeId?.takeIf { id -> ArtStyles.all.any { it.id == id } } ?: ArtStyles.DEFAULT_ID
            val restored = saved?.state
            if (restored != null) {
                gameState = restored
                if (gameState.board.score > best) best = gameState.board.score
                publish()
            } else {
                gameState = engine.newGame()
                publish(animateSpawns = true)
                persist()
            }
        }
    }

    fun swipe(direction: Direction) {
        val snapshot = _ui.value
        if (snapshot.loading || snapshot.busy) return
        if (gameState.board.status == Status.Won || gameState.board.status == Status.Lost) return
        val outcome = engine.move(gameState, direction)
        if (!outcome.moved) return
        gameState = outcome.state
        if (gameState.board.score > best) best = gameState.board.score
        swipeSerial += 1
        publish(slides = outcome.slides, spawned = outcome.spawned)
        persist()
    }

    fun undo() {
        if (_ui.value.loading || !gameState.canUndo) return
        gameState = engine.undo(gameState)
        publish()
        persist()
    }

    fun requestNewGame() {
        if (_ui.value.loading) return
        _ui.update { it.copy(confirmNewGame = true) }
    }

    fun dismissNewGame() {
        _ui.update { it.copy(confirmNewGame = false) }
    }

    fun confirmNewGame() {
        if (_ui.value.loading) return
        gameState = engine.newGame()
        _ui.update { it.copy(confirmNewGame = false) }
        publish(animateSpawns = true)
        persist()
    }

    fun continueAfterWin() {
        gameState = engine.continueAfterWin(gameState)
        publish()
        persist()
    }

    fun setTheme(id: String) {
        if (ArtStyles.all.none { it.id == id } || id == themeId) return
        themeId = id
        _ui.update { it.copy(themeId = id) }
        persist()
    }

    private fun publish(
        slides: List<Slide> = emptyList(),
        spawned: Spawn? = null,
        animateSpawns: Boolean = false,
    ) {
        moveSerial += 1
        val serial = moveSerial
        val previous = _ui.value.tiles
        val tiles = buildTiles(gameState.board, slides, spawned, serial, animateSpawns, previous)
        val ghosts = slides.filter { it.consumed }.map { slide ->
            GhostVisual(
                id = slide.id,
                value = slide.fromValue,
                fromRow = slide.fromRow,
                fromCol = slide.fromCol,
                toRow = slide.toRow,
                toCol = slide.toCol,
                moveId = serial,
            )
        }
        val animating = slides.isNotEmpty() || animateSpawns
        _ui.value = _ui.value.copy(
            loading = false,
            tiles = tiles,
            ghosts = ghosts,
            score = gameState.board.score,
            best = best,
            canUndo = gameState.canUndo,
            status = gameState.board.status,
            themeId = themeId,
            busy = animating,
            moveSerial = serial,
            swipeSerial = swipeSerial,
        )
        if (animating) {
            viewModelScope.launch {
                delay(ANIMATION_MS)
                _ui.update { current ->
                    if (current.moveSerial == serial) current.copy(ghosts = emptyList(), busy = false) else current
                }
            }
        }
    }

    private fun persist() {
        val snapshot = gameState
        val bestNow = best
        val themeNow = themeId
        viewModelScope.launch {
            persistMutex.withLock {
                try {
                    repository.save(snapshot, bestNow, themeNow)
                } catch (_: Throwable) {
                    // The in-memory board stays playable if the disk write fails.
                }
            }
        }
    }

    companion object {
        const val ANIMATION_MS = 200L

        fun factory(repository: Game2048Repository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return Game2048ViewModel(repository) as T
                }
            }
    }
}

private fun buildTiles(
    board: Board,
    slides: List<Slide>,
    spawned: Spawn?,
    serial: Long,
    animateSpawns: Boolean,
    previous: List<TileVisual>,
): List<TileVisual> {
    val slideById = slides.filter { !it.consumed }.associateBy { it.id }
    val previousMove = previous.associate { it.id to it.moveId }
    return board.cells.mapIndexedNotNull { index, tile ->
        if (tile == null) return@mapIndexedNotNull null
        val row = index / board.size
        val col = index % board.size
        val slide = slideById[tile.id]
        val isSpawn = animateSpawns || spawned?.id == tile.id
        val changed = slide != null || isSpawn
        TileVisual(
            id = tile.id,
            value = tile.value,
            row = row,
            col = col,
            fromRow = if (isSpawn) row else slide?.fromRow ?: row,
            fromCol = if (isSpawn) col else slide?.fromCol ?: col,
            spawned = isSpawn,
            merged = slide?.let { it.toValue != it.fromValue } == true,
            moveId = if (changed) serial else previousMove[tile.id] ?: serial,
        )
    }
}
