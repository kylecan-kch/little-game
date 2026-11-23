package com.gamecollection.game2048

import android.util.Log
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
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
    private val pendingSwipes = ArrayDeque<Direction>()
    private var gameState = GameState(Board.empty())
    private var best = 0
    private var themeId = ArtStyles.DEFAULT_ID
    private var moveSerial = 0L
    private var swipeSerial = 0L
    private var writesEnabled = true
    private var reportedWriteSkip = false

    init {
        viewModelScope.launch {
            var readFailed = false
            val saved = try {
                repository.load()
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                Log.e(TAG, "Failed to read 2048 save", t)
                readFailed = true
                null
            }
            writesEnabled = !readFailed
            val plan = planStartup(
                readFailed = readFailed,
                savedState = saved?.state,
                savedBest = saved?.best ?: 0,
                savedThemeId = saved?.themeId,
                themeIds = ArtStyles.all.map { it.id }.toSet(),
                defaultThemeId = ArtStyles.DEFAULT_ID,
                normalize = engine::normalize,
            )
            best = plan.best
            themeId = plan.themeId
            val restored = plan.restored
            if (restored != null) {
                gameState = restored
                publish()
            } else {
                gameState = engine.newGame()
                publish(animateSpawns = true)
            }
            if (plan.persist) persist()
        }
    }

    fun swipe(direction: Direction) {
        if (_ui.value.loading) return
        if (isTerminal()) {
            pendingSwipes.clear()
            return
        }
        if (_ui.value.busy) {
            pendingSwipes.addLast(direction)
            return
        }
        performSwipe(direction)
    }

    fun undo() {
        pendingSwipes.clear()
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
        pendingSwipes.clear()
        gameState = engine.newGame()
        _ui.update { it.copy(confirmNewGame = false) }
        publish(animateSpawns = true)
        persist()
    }

    fun continueAfterWin() {
        pendingSwipes.clear()
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

    private fun isTerminal(): Boolean {
        val status = gameState.board.status
        return status == Status.Won || status == Status.Lost
    }

    private fun performSwipe(direction: Direction) {
        var current: Direction? = direction
        while (current != null) {
            if (_ui.value.loading || isTerminal()) {
                pendingSwipes.clear()
                return
            }
            val outcome = engine.move(gameState, current)
            if (outcome.moved) {
                gameState = outcome.state
                if (gameState.board.score > best) best = gameState.board.score
                swipeSerial += 1
                if (isTerminal()) pendingSwipes.clear()
                publish(slides = outcome.slides, spawned = outcome.spawned)
                persist()
                return
            }
            current = pendingSwipes.removeFirstOrNull()
        }
    }

    private fun pumpQueue() {
        if (_ui.value.loading || _ui.value.busy) return
        if (isTerminal()) {
            pendingSwipes.clear()
            return
        }
        val next = pendingSwipes.removeFirstOrNull() ?: return
        performSwipe(next)
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
                val settled = _ui.updateAndGet { current ->
                    if (current.moveSerial != serial) {
                        current
                    } else {
                        current.copy(
                            tiles = current.tiles.map { tile ->
                                tile.copy(
                                    fromRow = tile.row,
                                    fromCol = tile.col,
                                    spawned = false,
                                    merged = false,
                                )
                            },
                            ghosts = emptyList(),
                            busy = false,
                        )
                    }
                }
                if (settled.moveSerial == serial && !settled.busy) pumpQueue()
            }
        }
    }

    private fun persist() {
        if (!writesEnabled) {
            if (!reportedWriteSkip) {
                reportedWriteSkip = true
                Log.w(TAG, "Not writing the 2048 save because the existing one could not be read")
            }
            return
        }
        val snapshot = gameState
        val bestNow = best
        val themeNow = themeId
        viewModelScope.launch {
            persistMutex.withLock {
                try {
                    repository.save(snapshot, bestNow, themeNow)
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    Log.e(TAG, "Failed to save 2048 game", t)
                }
            }
        }
    }

    companion object {
        const val ANIMATION_MS = 200L
        private const val TAG = "Game2048"

        fun factory(repository: Game2048Repository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return Game2048ViewModel(repository) as T
                }
            }
    }
}

internal data class StartupPlan(
    val best: Int,
    val themeId: String,
    val restored: GameState?,
    val persist: Boolean,
)

/**
 * A failed read must not be treated as an empty save: doing so would replace a
 * good file, including the best score, with a new game. A missing state is a
 * first launch (or an undecodable payload) and may be persisted.
 */
internal fun planStartup(
    readFailed: Boolean,
    savedState: GameState?,
    savedBest: Int,
    savedThemeId: String?,
    themeIds: Set<String>,
    defaultThemeId: String,
    normalize: (GameState) -> GameState,
): StartupPlan {
    if (readFailed) {
        return StartupPlan(
            best = 0,
            themeId = defaultThemeId,
            restored = null,
            persist = false,
        )
    }
    val themeId = savedThemeId?.takeIf { it in themeIds } ?: defaultThemeId
    val normalized = savedState?.let(normalize)
    val storedBest = savedBest.coerceAtLeast(0)
    val best = if (normalized != null) maxOf(storedBest, normalized.board.score) else storedBest
    val persist = normalized == null || normalized != savedState || best != storedBest
    return StartupPlan(
        best = best,
        themeId = themeId,
        restored = normalized,
        persist = persist,
    )
}

internal fun buildTiles(
    board: Board,
    slides: List<Slide>,
    spawned: Spawn?,
    serial: Long,
    animateSpawns: Boolean,
    previous: List<TileVisual>,
): List<TileVisual> {
    val slideById = slides.filter { !it.consumed }.associateBy { it.id }
    val previousById = previous.associateBy { it.id }
    return board.cells.mapIndexedNotNull { index, tile ->
        if (tile == null) return@mapIndexedNotNull null
        val row = index / board.size
        val col = index % board.size
        val slide = slideById[tile.id]
        val isSpawn = animateSpawns || spawned?.id == tile.id
        val prior = previousById[tile.id]
        val positionChanged = prior != null && (prior.row != row || prior.col != col)
        val changed = slide != null || isSpawn || positionChanged
        TileVisual(
            id = tile.id,
            value = tile.value,
            row = row,
            col = col,
            fromRow = if (isSpawn) row else slide?.fromRow ?: row,
            fromCol = if (isSpawn) col else slide?.fromCol ?: col,
            spawned = isSpawn,
            merged = slide?.let { it.toValue != it.fromValue } == true,
            moveId = if (changed) serial else prior?.moveId ?: serial,
        )
    }
}
