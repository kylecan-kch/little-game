package com.gamecollection.game2048.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gamecollection.game2048.R
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.gamecollection.engine2048.Direction
import com.gamecollection.game2048.GhostVisual
import com.gamecollection.game2048.TileVisual
import com.gamecollection.game2048.theme.ArtStyle
import kotlin.math.abs
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameBoard(
    style: ArtStyle,
    tiles: List<TileVisual>,
    ghosts: List<GhostVisual>,
    enabled: Boolean,
    boardDescription: String,
    onSwipe: (Direction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gesturesEnabled by rememberUpdatedState(enabled)
    val swipe by rememberUpdatedState(onSwipe)
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth, maxHeight)
        val fit = fitBoard(side, style.gridPadding, style.gridGap) ?: return@BoxWithConstraints
        val cell = fit.cell
        val gap = fit.gap
        val pad = fit.pad
        val metrics = cellMetrics(cell, gap, pad)
        Box(
            Modifier
                .size(side)
                .clip(RoundedCornerShape(style.chrome.corner))
                .boardSurface(style)
                .semantics { contentDescription = boardDescription }
                .pointerInput(Unit) {
                    var total = Offset.Zero
                    detectDragGestures(
                        onDragStart = { total = Offset.Zero },
                        onDrag = { change, dragAmount ->
                            total += dragAmount
                            change.consume()
                        },
                        onDragEnd = {
                            if (!gesturesEnabled) return@detectDragGestures
                            val threshold = 36.dp.toPx()
                            val dx = total.x
                            val dy = total.y
                            if (abs(dx) < threshold && abs(dy) < threshold) return@detectDragGestures
                            val direction = if (abs(dx) > abs(dy)) {
                                if (dx > 0f) Direction.Right else Direction.Left
                            } else {
                                if (dy > 0f) Direction.Down else Direction.Up
                            }
                            swipe(direction)
                        },
                    )
                },
        ) {
            for (row in 0 until 4) {
                for (col in 0 until 4) {
                    EmptyCell(
                        style = style,
                        modifier = Modifier
                            .offset(x = pad + (cell + gap) * col, y = pad + (cell + gap) * row)
                            .size(cell),
                    )
                }
            }
            ghosts.forEach { ghost ->
                key("ghost-${ghost.id}-${ghost.moveId}") {
                    GhostTile(style, ghost, metrics, cell)
                }
            }
            tiles.forEach { tile ->
                key(tile.id) {
                    MovingTile(style, tile, metrics, cell)
                }
            }
        }
    }
}

@Composable
private fun MovingTile(
    style: ArtStyle,
    tile: TileVisual,
    metrics: CellMetrics,
    cell: Dp,
) {
    val x = remember(tile.id) { Animatable(metrics.paddingPx + tile.fromCol * metrics.stridePx) }
    val y = remember(tile.id) { Animatable(metrics.paddingPx + tile.fromRow * metrics.stridePx) }
    val scale = remember(tile.id) { Animatable(if (tile.spawned) 0.15f else 1f) }
    LaunchedEffect(tile.moveId, tile.row, tile.col, metrics.stridePx, metrics.paddingPx) {
        val targetX = metrics.paddingPx + tile.col * metrics.stridePx
        val targetY = metrics.paddingPx + tile.row * metrics.stridePx
        if (tile.spawned || (tile.fromCol == tile.col && tile.fromRow == tile.row)) {
            x.snapTo(targetX)
            y.snapTo(targetY)
        } else {
            x.snapTo(metrics.paddingPx + tile.fromCol * metrics.stridePx)
            y.snapTo(metrics.paddingPx + tile.fromRow * metrics.stridePx)
            coroutineScope {
                launch { x.animateTo(targetX, tween(120, easing = FastOutSlowInEasing)) }
                launch { y.animateTo(targetY, tween(120, easing = FastOutSlowInEasing)) }
            }
        }
    }
    LaunchedEffect(tile.moveId) {
        if (tile.spawned) {
            scale.snapTo(0.15f)
            scale.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 420f))
        } else if (tile.merged) {
            delay(80)
            scale.animateTo(1.12f, tween(60))
            scale.animateTo(1f, tween(70))
        } else {
            scale.snapTo(1f)
        }
    }
    ArtTile(
        style = style,
        value = tile.value,
        cellSize = cell,
        contentDescription = stringResource(R.string.cd_tile, tile.value),
        modifier = Modifier
            .zIndex(if (tile.merged) 1f else 0f)
            .graphicsLayer {
                translationX = x.value
                translationY = y.value
                scaleX = scale.value
                scaleY = scale.value
            }
            .size(cell),
    )
}

@Composable
private fun GhostTile(
    style: ArtStyle,
    ghost: GhostVisual,
    metrics: CellMetrics,
    cell: Dp,
) {
    val progress = remember(ghost.moveId) { Animatable(0f) }
    LaunchedEffect(ghost.moveId) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(130, easing = FastOutSlowInEasing))
    }
    val fromX = metrics.paddingPx + ghost.fromCol * metrics.stridePx
    val fromY = metrics.paddingPx + ghost.fromRow * metrics.stridePx
    val toX = metrics.paddingPx + ghost.toCol * metrics.stridePx
    val toY = metrics.paddingPx + ghost.toRow * metrics.stridePx
    ArtTile(
        style = style,
        value = ghost.value,
        cellSize = cell,
        modifier = Modifier
            .zIndex(0.5f)
            .graphicsLayer {
                val amount = progress.value
                translationX = lerp(fromX, toX, amount)
                translationY = lerp(fromY, toY, amount)
                alpha = 1f - amount
                val shrink = 1f - 0.15f * amount
                scaleX = shrink
                scaleY = shrink
            }
            .size(cell),
    )
}

private data class BoardFit(val cell: Dp, val gap: Dp, val pad: Dp)

/** Keeps every cell positive when padding and gaps would otherwise consume the board. */
private fun fitBoard(side: Dp, pad: Dp, gap: Dp): BoardFit? {
    if (!side.value.isFinite() || side <= 0.dp) return null
    val chrome = pad * 2 + gap * 3
    if (side > chrome) {
        val cell = (side - chrome) / 4
        if (cell > 0.dp) return BoardFit(cell, gap, pad)
    }
    val cell = side / 4
    if (cell <= 0.dp) return null
    return BoardFit(cell = cell, gap = 0.dp, pad = 0.dp)
}
