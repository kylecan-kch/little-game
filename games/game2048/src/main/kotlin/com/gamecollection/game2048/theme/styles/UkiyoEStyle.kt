package com.gamecollection.game2048.theme.styles

import androidx.compose.ui.geometry.Offset
import com.gamecollection.game2048.theme.CanvasSize
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamecollection.game2048.theme.ArtChrome
import com.gamecollection.game2048.theme.ArtStyle
import com.gamecollection.game2048.theme.onColor
import com.gamecollection.game2048.theme.tierOf
import com.gamecollection.game2048.theme.wavePath

object UkiyoEStyle : ArtStyle() {
    override val id: String = "ukiyo"
    override val name: String = "浮世绘"
    override val blurb: String = "靛蓝浪线、朱印和木版黑边"
    override val fontFamily: FontFamily = FontFamily.Serif
    override val numberWeight: FontWeight = FontWeight.Black
    override val gridGap = 6.dp
    override val gridPadding = 8.dp
    override val chrome = ArtChrome(
        textPrimary = Color(0xFF1A1A1A),
        textMuted = Color(0xFF5C5348),
        accent = Color(0xFFC4372C),
        onAccent = Color(0xFFFFF6E8),
        panel = Color(0xFFF3E6CF),
        onPanel = Color(0xFF1A1A1A),
        outline = Color(0xFF151515),
        corner = 4.dp,
        buttonCorner = 2.dp,
    )

    override fun numberColor(value: Int): Color = onColor(fillFor(value))

    override fun prepareBackdrop(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val bands = List(8) { index ->
            wavePath(size.width, size.height * (0.18f + index * 0.1f), size.height * 0.035f, if (index % 2 == 0) 8f else -6f)
        }
        return {
            drawRect(Color(0xFFF6E7D0))
            drawCircle(Color(0xFFE7D7B1), size.minDimension * 0.16f, Offset(size.width * 0.78f, size.height * 0.16f))
            bands.forEachIndexed { index, path ->
                val color = if (index % 2 == 0) Color(0xFF1F4E79) else Color(0xFF7FA6C4)
                drawPath(path, color, style = Stroke(width = size.minDimension * 0.012f))
            }
        }
    }

    override fun prepareBoard(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        return {
            drawRect(Color(0xFFF8EBD4))
            drawRect(Color(0xFF161616), style = Stroke(width = size.minDimension * 0.02f))
        }
    }

    override fun prepareEmpty(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        return {
            drawRect(Color(0xFFE7D3B0))
            drawRect(Color(0xFF161616), style = Stroke(width = size.minDimension * 0.035f))
        }
    }

    override fun prepareTile(scope: CanvasSize, value: Int): DrawScope.() -> Unit = with(scope) {
        val fill = fillFor(value)
        val bands = List(4) { index ->
            wavePath(
                size.width,
                size.height * (0.22f + index * 0.16f),
                size.height * 0.06f,
                if (index % 2 == 0) 4f else -4f,
            )
        }
        val sun = tierOf(value) >= 6
        return {
            drawRect(fill)
            bands.forEachIndexed { index, path ->
                val ink = if (index % 2 == 0) Color(0xFFF7F1E4) else Color(0xFF163E68)
                drawPath(path, ink.copy(alpha = 0.9f), style = Stroke(width = size.minDimension * 0.035f))
            }
            drawRect(Color(0xFF141414), style = Stroke(width = size.minDimension * 0.055f))
            if (sun) {
                drawCircle(Color(0xFFC4372C), size.minDimension * 0.12f, Offset(size.width * 0.78f, size.height * 0.24f))
            }
        }
    }

    private fun fillFor(value: Int): Color = fills[tierOf(value) % fills.size]

    private val fills = listOf(
        Color(0xFFE7D7B8),
        Color(0xFFD5E2EA),
        Color(0xFF8FB4D4),
        Color(0xFF1F4E79),
        Color(0xFF16324F),
        Color(0xFF114B63),
        Color(0xFFC4372C),
        Color(0xFF0E2A44),
        Color(0xFFE6B15A),
        Color(0xFF111111),
        Color(0xFF9E2B24),
    )
}
