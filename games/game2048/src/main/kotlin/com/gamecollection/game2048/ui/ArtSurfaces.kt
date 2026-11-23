package com.gamecollection.game2048.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import com.gamecollection.game2048.theme.ArtStyle
import com.gamecollection.game2048.theme.ArtStyles
import com.gamecollection.game2048.theme.CanvasSize

@Composable
fun ArtBackdrop(style: ArtStyle, modifier: Modifier = Modifier) {
    Spacer(
        modifier.drawWithCache {
            val paint = style.prepareBackdrop(CanvasSize(size))
            onDrawBehind { paint() }
        },
    )
}

@Composable
fun ArtTile(
    style: ArtStyle,
    value: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    BoxWithConstraints(
        modifier = modifier.then(
            if (contentDescription != null) {
                Modifier.semantics { this.contentDescription = contentDescription }
            } else {
                Modifier
            },
        ),
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            Modifier
                .fillMaxSize()
                .drawWithCache {
                    val paint = style.prepareTile(CanvasSize(size), value)
                    onDrawBehind { paint() }
                },
        )
        if (!style.drawsOwnNumbers) {
            val digits = value.toString().length
            val factor = when {
                digits <= 2 -> 0.42f
                digits == 3 -> 0.32f
                digits == 4 -> 0.24f
                else -> 0.18f
            }
            val fontSize = (maxWidth.value * factor).sp
            Text(
                text = value.toString(),
                color = style.numberColor(value),
                fontFamily = style.fontFamily,
                fontWeight = style.numberWeight,
                fontSize = fontSize,
                letterSpacing = style.numberLetterSpacing,
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = TextStyle(
                    shadow = style.numberShadow(value),
                    lineHeight = fontSize,
                ),
            )
        }
    }
}

@Composable
fun ThemePreview(style: ArtStyle, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(style.chrome.corner.coerceAtMost(16.dp))),
    ) {
        ArtBackdrop(style, Modifier.matchParentSize())
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
        ) {
            val tile = minOf(maxWidth, maxHeight) * 0.36f
            val gap = tile * 0.12f
            val originX = (maxWidth - tile * 2 - gap) / 2
            val originY = (maxHeight - tile * 2 - gap) / 2
            ArtStyles.previewValues.forEachIndexed { index, value ->
                val column = index % 2
                val row = index / 2
                ArtTile(
                    style = style,
                    value = value,
                    modifier = Modifier
                        .offset(x = originX + (tile + gap) * column, y = originY + (tile + gap) * row)
                        .size(tile),
                )
            }
        }
    }
}

internal fun Modifier.boardSurface(style: ArtStyle): Modifier = drawWithCache {
    val paint = style.prepareBoard(CanvasSize(size))
    onDrawBehind { paint() }
}

@Composable
internal fun EmptyCell(style: ArtStyle, modifier: Modifier = Modifier) {
    Spacer(
        modifier.drawWithCache {
            val paint = style.prepareEmpty(CanvasSize(size))
            onDrawBehind { paint() }
        },
    )
}

@Composable
internal fun cellMetrics(cell: Dp, gap: Dp, padding: Dp): CellMetrics {
    val density = LocalDensity.current
    return with(density) {
        CellMetrics(
            cellPx = cell.toPx(),
            stridePx = (cell + gap).toPx(),
            paddingPx = padding.toPx(),
        )
    }
}

internal data class CellMetrics(
    val cellPx: Float,
    val stridePx: Float,
    val paddingPx: Float,
)
