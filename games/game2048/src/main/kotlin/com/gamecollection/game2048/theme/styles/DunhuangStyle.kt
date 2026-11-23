package com.gamecollection.game2048.theme.styles

import androidx.compose.ui.geometry.CornerRadius
import com.gamecollection.game2048.theme.CanvasSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamecollection.game2048.theme.ArtChrome
import com.gamecollection.game2048.theme.ArtStyle
import com.gamecollection.game2048.theme.specks
import com.gamecollection.game2048.theme.tierOf
import kotlin.random.Random

object DunhuangStyle : ArtStyle() {
    override val id: String = "dunhuang"
    override val name: String = "敦煌壁画"
    override val blurb: String = "矿物色、金边和莲瓣"
    override val fontFamily: FontFamily = FontFamily.Serif
    override val numberWeight: FontWeight = FontWeight.Bold
    override val gridGap = 8.dp
    override val gridPadding = 10.dp
    override val chrome = ArtChrome(
        textPrimary = Color(0xFF3A2716),
        textMuted = Color(0xFF7A5A3A),
        accent = Color(0xFF9C2B24),
        onAccent = Color(0xFFF8E7C4),
        panel = Color(0xFFE7C99A),
        onPanel = Color(0xFF3A2716),
        outline = Color(0xFFC4963A),
        corner = 8.dp,
        buttonCorner = 6.dp,
    )

    private val plaster = Color(0xFFC9A36A)
    private val gold = Color(0xFFD4A84B)
    private val cinnabar = Color(0xFF9C2B24)
    private val azurite = Color(0xFF2C5F8A)
    private val malachite = Color(0xFF2F7A62)
    private val chalk = Color(0xFFF3E6C8)
    private val ink = Color(0xFF3A2716)

    override fun numberColor(value: Int): Color {
        val fill = fills[tierOf(value) % fills.size]
        val luminance = 0.299f * fill.red + 0.587f * fill.green + 0.114f * fill.blue
        return if (luminance > 0.62f) ink else gold
    }

    override fun prepareBackdrop(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val grain = specks(size.width, size.height, 15, 50, ink.copy(alpha = 0.15f), 1.8f)
        val cracks = cracks(size.width, size.height, 12)
        return {
            drawRect(plaster)
            grain.forEach { drawCircle(it.color, it.radius, it.center) }
            cracks.forEach { path ->
                drawPath(path, ink.copy(alpha = 0.28f), style = Stroke(width = 1.4f))
            }
            drawCircle(cinnabar.copy(alpha = 0.18f), size.minDimension * 0.2f, Offset(size.width * 0.2f, size.height * 0.18f))
            drawCircle(azurite.copy(alpha = 0.16f), size.minDimension * 0.16f, Offset(size.width * 0.78f, size.height * 0.72f))
        }
    }

    override fun prepareBoard(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val radius = CornerRadius(12f, 12f)
        return {
            drawRoundRect(Color(0xFFB48958), cornerRadius = radius)
            drawRoundRect(cinnabar, cornerRadius = radius, style = Stroke(width = size.minDimension * 0.018f))
            drawRoundRect(
                gold,
                topLeft = Offset(size.minDimension * 0.02f, size.minDimension * 0.02f),
                size = Size(size.width - size.minDimension * 0.04f, size.height - size.minDimension * 0.04f),
                cornerRadius = radius,
                style = Stroke(width = size.minDimension * 0.008f),
            )
        }
    }

    override fun prepareEmpty(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val radius = CornerRadius(size.minDimension * 0.12f, size.minDimension * 0.12f)
        return {
            drawRoundRect(Color(0xFFB48958).copy(alpha = 0.55f), cornerRadius = radius)
            drawRoundRect(gold.copy(alpha = 0.7f), cornerRadius = radius, style = Stroke(width = 1.5f))
        }
    }

    override fun prepareTile(scope: CanvasSize, value: Int): DrawScope.() -> Unit = with(scope) {
        val fill = fills[tierOf(value) % fills.size]
        val radius = CornerRadius(size.minDimension * 0.1f, size.minDimension * 0.1f)
        val petalW = size.width * 0.16f
        val petalH = size.height * 0.28f
        return {
            drawRoundRect(fill, cornerRadius = radius)
            val cx = size.width / 2f
            val cy = size.height / 2f
            repeat(8) { index ->
                rotate(index * 45f, Offset(cx, cy)) {
                    drawOval(
                        gold.copy(alpha = 0.9f),
                        topLeft = Offset(cx - petalW / 2f, cy - size.height * 0.46f),
                        size = Size(petalW, petalH),
                    )
                }
            }
            drawCircle(fill, size.minDimension * 0.3f, Offset(cx, cy))
            drawCircle(chalk.copy(alpha = 0.35f), size.minDimension * 0.3f, Offset(cx, cy), style = Stroke(width = 2f))
            drawRoundRect(gold, cornerRadius = radius, style = Stroke(width = size.minDimension * 0.035f))
            drawRoundRect(
                cinnabar,
                topLeft = Offset(size.minDimension * 0.045f, size.minDimension * 0.045f),
                size = Size(size.width - size.minDimension * 0.09f, size.height - size.minDimension * 0.09f),
                cornerRadius = radius,
                style = Stroke(width = size.minDimension * 0.012f),
            )
        }
    }

    private fun cracks(width: Float, height: Float, count: Int): List<Path> {
        val random = Random(48)
        return List(count) {
            val path = Path()
            var x = random.nextFloat() * width
            var y = random.nextFloat() * height
            path.moveTo(x, y)
            repeat(4) {
                x += (random.nextFloat() - 0.5f) * width * 0.18f
                y += (random.nextFloat() - 0.3f) * height * 0.12f
                path.lineTo(x, y)
            }
            path
        }
    }

    private val fills = listOf(
        chalk,
        Color(0xFFE0B56A),
        malachite,
        azurite,
        cinnabar,
        gold,
        Color(0xFF1E4C78),
        Color(0xFF1F5C49),
        Color(0xFF7E1E1A),
        Color(0xFFE6C56A),
        Color(0xFF143E66),
    )
}
