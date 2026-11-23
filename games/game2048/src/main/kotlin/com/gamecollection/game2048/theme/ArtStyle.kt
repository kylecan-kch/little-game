package com.gamecollection.game2048.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ArtChrome(
    val textPrimary: Color,
    val textMuted: Color,
    val accent: Color,
    val onAccent: Color,
    val panel: Color,
    val onPanel: Color,
    val outline: Color,
    val corner: Dp,
    val buttonCorner: Dp,
    val scrim: Color = Color(0xB3000000),
)

/**
 * One art direction for the whole 2048 screen.
 *
 * Geometry is built in the prepare methods and captured by the returned draw lambda,
 * so swipes only move tiles and do not rebuild brushes. To add a style, implement
 * this class and register the object in [ArtStyles.all].
 */
@Stable
abstract class ArtStyle {
    abstract val id: String
    abstract val name: String
    abstract val blurb: String
    abstract val chrome: ArtChrome
    abstract val fontFamily: FontFamily
    open val numberWeight: FontWeight = FontWeight.Bold
    open val drawsOwnNumbers: Boolean = false
    open val lightStatusIcons: Boolean = false
    open val gridGap: Dp = 8.dp
    open val gridPadding: Dp = 10.dp
    open val numberLetterSpacing: TextUnit = 0.sp

    abstract fun prepareBackdrop(scope: CanvasSize): DrawScope.() -> Unit
    abstract fun prepareBoard(scope: CanvasSize): DrawScope.() -> Unit
    abstract fun prepareEmpty(scope: CanvasSize): DrawScope.() -> Unit
    abstract fun prepareTile(scope: CanvasSize, value: Int): DrawScope.() -> Unit

    open fun numberColor(value: Int): Color = chrome.textPrimary
    open fun numberShadow(value: Int): Shadow? = null
}

/** Size available while a draw cache is built, before the draw lambda runs. */
class CanvasSize(val size: Size) {
    val center: Offset = Offset(size.width / 2f, size.height / 2f)
}
