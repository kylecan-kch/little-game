package com.gamecollection.game2048.theme.styles

import androidx.compose.ui.geometry.Offset
import com.gamecollection.game2048.theme.CanvasSize
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.gamecollection.game2048.theme.ArtChrome
import com.gamecollection.game2048.theme.ArtStyle
import com.gamecollection.game2048.theme.onColor
import com.gamecollection.game2048.theme.specks
import com.gamecollection.game2048.theme.tierOf
import kotlin.random.Random

object WatercolorStyle : ArtStyle() {
    override val id: String = "watercolor"
    override val name: String = "水彩画"
    override val blurb: String = "湿纸上晕开的透明颜料"
    override val fontFamily: FontFamily = FontFamily.Cursive
    override val gridGap = 10.dp
    override val gridPadding = 12.dp
    override val chrome = ArtChrome(
        textPrimary = Color(0xFF2E3C55),
        textMuted = Color(0xFF6D7890),
        accent = Color(0xFF3E7C9A),
        onAccent = Color(0xFFF7FBFF),
        panel = Color(0xFFF7F4EE),
        onPanel = Color(0xFF2E3C55),
        outline = Color(0xFF3E7C9A),
        corner = 22.dp,
        buttonCorner = 24.dp,
    )

    override fun numberColor(value: Int): Color = onColor(palette[tierOf(value) % palette.size])

    override fun prepareBackdrop(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val blooms = blooms(size.width, size.height, 21, 14, size.minDimension * 0.28f)
        val grain = specks(size.width, size.height, 8, 60, Color(0xFF8D8478).copy(alpha = 0.16f), 1.4f)
        return {
            drawRect(Color(0xFFFBF7F2))
            grain.forEach { drawCircle(it.color, it.radius, it.center) }
            blooms.forEach { bloom -> drawBloom(bloom) }
        }
    }

    override fun prepareBoard(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val blooms = blooms(size.width, size.height, 44, 8, size.minDimension * 0.22f)
        return {
            drawRect(Color(0xFFF3E6DC).copy(alpha = 0.72f))
            blooms.forEach { bloom -> drawBloom(bloom) }
        }
    }

    override fun prepareEmpty(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val bloom = Bloom(
            center = center,
            radius = size.minDimension * 0.34f,
            color = Color(0xFFD7C2B0),
            alpha = 0.35f,
        )
        return { drawBloom(bloom) }
    }

    override fun prepareTile(scope: CanvasSize, value: Int): DrawScope.() -> Unit = with(scope) {
        val base = palette[tierOf(value) % palette.size]
        val random = Random(value * 9 + 2)
        val puddles = List(5) {
            Bloom(
                center = Offset(
                    size.width * (0.25f + random.nextFloat() * 0.5f),
                    size.height * (0.25f + random.nextFloat() * 0.5f),
                ),
                radius = size.minDimension * (0.22f + random.nextFloat() * 0.28f),
                color = if (it % 2 == 0) base else palette[(tierOf(value) + 2) % palette.size],
                alpha = 0.28f + random.nextFloat() * 0.28f,
            )
        }
        return {
            puddles.forEach { bloom -> drawBloom(bloom) }
            drawOval(
                base.copy(alpha = 0.45f),
                topLeft = Offset(size.width * 0.12f, size.height * 0.14f),
                size = Size(size.width * 0.76f, size.height * 0.72f),
                style = Stroke(width = size.minDimension * 0.045f),
            )
        }
    }

    private data class Bloom(val center: Offset, val radius: Float, val color: Color, val alpha: Float)

    private fun blooms(width: Float, height: Float, seed: Int, count: Int, maxRadius: Float): List<Bloom> {
        val random = Random(seed)
        return List(count) { index ->
            Bloom(
                center = Offset(random.nextFloat() * width, random.nextFloat() * height),
                radius = maxRadius * (0.45f + random.nextFloat()),
                color = palette[index % palette.size],
                alpha = 0.16f + random.nextFloat() * 0.2f,
            )
        }
    }

    private fun DrawScope.drawBloom(bloom: Bloom) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(bloom.color.copy(alpha = bloom.alpha), bloom.color.copy(alpha = bloom.alpha * 0.25f), Color.Transparent),
                center = bloom.center,
                radius = bloom.radius,
            ),
            radius = bloom.radius,
            center = bloom.center,
        )
    }

    private val palette = listOf(
        Color(0xFFF6E7A1),
        Color(0xFFF3C1A0),
        Color(0xFFE98B84),
        Color(0xFFD98AB8),
        Color(0xFF8EB8D8),
        Color(0xFF7ECAE0),
        Color(0xFF8FCB9B),
        Color(0xFFE2C56B),
        Color(0xFF6F8FBF),
        Color(0xFFD15B63),
        Color(0xFF355C7D),
    )
}
