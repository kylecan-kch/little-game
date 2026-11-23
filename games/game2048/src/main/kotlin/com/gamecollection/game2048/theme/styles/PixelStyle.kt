package com.gamecollection.game2048.theme.styles

import androidx.compose.ui.geometry.Offset
import com.gamecollection.game2048.theme.CanvasSize
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.gamecollection.game2048.theme.ArtChrome
import com.gamecollection.game2048.theme.ArtStyle
import com.gamecollection.game2048.theme.tierOf

object PixelStyle : ArtStyle() {
    override val id: String = "pixel"
    override val name: String = "像素复古"
    override val blurb: String = "八位机色板和方块字体"
    override val fontFamily: FontFamily = FontFamily.Monospace
    override val drawsOwnNumbers: Boolean = true
    override val lightStatusIcons: Boolean = true
    override val gridGap = 4.dp
    override val gridPadding = 6.dp
    override val chrome = ArtChrome(
        textPrimary = Color(0xFFF4F4F4),
        textMuted = Color(0xFF94B0C2),
        accent = Color(0xFFEF7D57),
        onAccent = Color(0xFF1A1C2C),
        panel = Color(0xFF1A1C2C),
        onPanel = Color(0xFFF4F4F4),
        outline = Color(0xFF73EFF7),
        corner = 0.dp,
        buttonCorner = 0.dp,
    )

    override fun prepareBackdrop(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val paint = crisp()
        val cell = (size.minDimension / 24f).coerceAtLeast(8f)
        return {
            checker(paint, cell, Color(0xFF1A1C2C), Color(0xFF29366F))
        }
    }

    override fun prepareBoard(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val paint = crisp()
        return {
            block(paint, Color(0xFF333C57), 0f, 0f, size.width, size.height)
            block(paint, Color(0xFF566C86), 0f, 0f, size.width, 6f)
            block(paint, Color(0xFF566C86), 0f, 0f, 6f, size.height)
            block(paint, Color(0xFF10121C), 0f, size.height - 6f, size.width, size.height)
            block(paint, Color(0xFF10121C), size.width - 6f, 0f, size.width, size.height)
        }
    }

    override fun prepareEmpty(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val paint = crisp()
        return { beveled(paint, Color(0xFF10121C), Color(0xFF566C86), Color(0xFF000000), size) }
    }

    override fun prepareTile(scope: CanvasSize, value: Int): DrawScope.() -> Unit = with(scope) {
        val paint = crisp()
        val pair = pairs[tierOf(value) % pairs.size]
        val digits = value.toString()
        return {
            beveled(paint, pair.first, pair.second, pair.third, size)
            pixelText(paint, digits, if (tierOf(value) % 5 == 3) Color(0xFF1A1C2C) else Color(0xFFF4F4F4), size)
        }
    }

    private data class Triple(val first: Color, val second: Color, val third: Color)

    private fun crisp() = Paint().apply {
        isAntiAlias = false
        style = PaintingStyle.Fill
    }

    private fun DrawScope.block(paint: Paint, color: Color, left: Float, top: Float, right: Float, bottom: Float) {
        paint.color = color
        drawContext.canvas.drawRect(left, top, right, bottom, paint)
    }

    private fun DrawScope.checker(paint: Paint, cell: Float, a: Color, b: Color) {
        var y = 0f
        var row = 0
        while (y < size.height) {
            var x = 0f
            var col = 0
            while (x < size.width) {
                block(paint, if ((row + col) % 2 == 0) a else b, x, y, x + cell, y + cell)
                x += cell
                col++
            }
            y += cell
            row++
        }
    }

    private fun DrawScope.beveled(paint: Paint, fill: Color, light: Color, dark: Color, size: Size) {
        val edge = (size.minDimension / 10f).coerceAtLeast(3f)
        block(paint, light, 0f, 0f, size.width, size.height)
        block(paint, dark, edge, edge, size.width, size.height)
        block(paint, fill, edge, edge, size.width - edge, size.height - edge)
        val step = edge
        var y = edge
        var row = 0
        while (y < size.height - edge) {
            var x = edge
            var col = 0
            while (x < size.width - edge) {
                if ((row + col) % 2 == 0) {
                    block(paint, light.copy(alpha = 0.35f), x, y, (x + step).coerceAtMost(size.width - edge), (y + step).coerceAtMost(size.height - edge))
                }
                x += step
                col++
            }
            y += step
            row++
        }
    }

    private fun DrawScope.pixelText(paint: Paint, text: String, color: Color, size: Size) {
        val glyphW = 5
        val glyphH = 7
        val gap = 1
        val total = text.length * glyphW + (text.length - 1) * gap
        val scale = (size.minDimension * 0.62f) / total.coerceAtLeast(glyphH)
        val originX = (size.width - total * scale) / 2f
        val originY = (size.height - glyphH * scale) / 2f
        text.forEachIndexed { index, char ->
            val rows = FONT[char] ?: return@forEachIndexed
            rows.forEachIndexed { row, bits ->
                for (col in 0 until glyphW) {
                    if ((bits shr (glyphW - 1 - col)) and 1 == 1) {
                        val left = originX + (index * (glyphW + gap) + col) * scale
                        val top = originY + row * scale
                        block(paint, color, left, top, left + scale + 0.5f, top + scale + 0.5f)
                    }
                }
            }
        }
    }

    private val pairs = listOf(
        Triple(Color(0xFF3B5DC9), Color(0xFF73EFF7), Color(0xFF1A1C2C)),
        Triple(Color(0xFF38B764), Color(0xFFA7F070), Color(0xFF1A1C2C)),
        Triple(Color(0xFFEF7D57), Color(0xFFFFCD75), Color(0xFF1A1C2C)),
        Triple(Color(0xFFFFCD75), Color(0xFFF4F4F4), Color(0xFFB13E53)),
        Triple(Color(0xFFB13E53), Color(0xFFEF7D57), Color(0xFF1A1C2C)),
        Triple(Color(0xFF5D275D), Color(0xFFB13E53), Color(0xFF1A1C2C)),
        Triple(Color(0xFF257179), Color(0xFF73EFF7), Color(0xFF1A1C2C)),
        Triple(Color(0xFF29366F), Color(0xFF41A6F6), Color(0xFF1A1C2C)),
        Triple(Color(0xFFEF7D57), Color(0xFFFFCD75), Color(0xFF5D275D)),
        Triple(Color(0xFF41A6F6), Color(0xFF73EFF7), Color(0xFF1A1C2C)),
        Triple(Color(0xFFFFCD75), Color(0xFFF4F4F4), Color(0xFF1A1C2C)),
    )

    private val FONT: Map<Char, IntArray> = mapOf(
        '0' to intArrayOf(0b01110, 0b10001, 0b10011, 0b10101, 0b11001, 0b10001, 0b01110),
        '1' to intArrayOf(0b00100, 0b01100, 0b00100, 0b00100, 0b00100, 0b00100, 0b01110),
        '2' to intArrayOf(0b01110, 0b10001, 0b00001, 0b00010, 0b00100, 0b01000, 0b11111),
        '3' to intArrayOf(0b11110, 0b00001, 0b00001, 0b01110, 0b00001, 0b00001, 0b11110),
        '4' to intArrayOf(0b00010, 0b00110, 0b01010, 0b10010, 0b11111, 0b00010, 0b00010),
        '5' to intArrayOf(0b11111, 0b10000, 0b11110, 0b00001, 0b00001, 0b10001, 0b01110),
        '6' to intArrayOf(0b01110, 0b10000, 0b11110, 0b10001, 0b10001, 0b10001, 0b01110),
        '7' to intArrayOf(0b11111, 0b00001, 0b00010, 0b00100, 0b01000, 0b01000, 0b01000),
        '8' to intArrayOf(0b01110, 0b10001, 0b10001, 0b01110, 0b10001, 0b10001, 0b01110),
        '9' to intArrayOf(0b01110, 0b10001, 0b10001, 0b01111, 0b00001, 0b00001, 0b01110),
    )
}
