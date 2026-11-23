package com.gamecollection.game2048.theme.styles

import androidx.compose.ui.geometry.CornerRadius
import com.gamecollection.game2048.theme.CanvasSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.gamecollection.game2048.theme.ArtChrome
import com.gamecollection.game2048.theme.ArtStyle
import com.gamecollection.game2048.theme.onColor
import com.gamecollection.game2048.theme.tierOf
import kotlin.random.Random

object MonetStyle : ArtStyle() {
    override val id: String = "monet"
    override val name: String = "莫奈水粉"
    override val blurb: String = "睡莲池上的短促笔触"
    override val fontFamily: FontFamily = FontFamily.Serif
    override val gridGap = 9.dp
    override val gridPadding = 12.dp
    override val chrome = ArtChrome(
        textPrimary = Color(0xFF24342C),
        textMuted = Color(0xFF5E7268),
        accent = Color(0xFF6E8F62),
        onAccent = Color(0xFFF7FBF4),
        panel = Color(0xFFE7F0E4),
        onPanel = Color(0xFF24342C),
        outline = Color(0xFF6E8F62),
        corner = 20.dp,
        buttonCorner = 20.dp,
    )

    override fun numberColor(value: Int): Color = onColor(palette[tierOf(value) % palette.size])

    override fun prepareBackdrop(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val dabs = dabs(size.width, size.height, 18, 90, size.minDimension * 0.035f)
        return {
            drawRect(Color(0xFFD5E3D4))
            dabs.forEach { dab -> drawDab(dab) }
        }
    }

    override fun prepareBoard(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val dabs = dabs(size.width, size.height, 27, 36, size.minDimension * 0.04f)
        val radius = CornerRadius(size.minDimension * 0.04f, size.minDimension * 0.04f)
        return {
            drawRoundRect(Color(0xFFC5D7C8), cornerRadius = radius)
            dabs.forEach { dab -> drawDab(dab) }
        }
    }

    override fun prepareEmpty(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val dabs = dabs(size.width, size.height, 6, 8, size.minDimension * 0.16f)
        return { dabs.forEach { dab -> drawDab(dab.copy(alpha = 0.28f)) } }
    }

    override fun prepareTile(scope: CanvasSize, value: Int): DrawScope.() -> Unit = with(scope) {
        val bias = tierOf(value)
        val dabs = dabs(size.width, size.height, value * 11 + 1, 22, size.minDimension * 0.16f, bias)
        return { dabs.forEach { dab -> drawDab(dab) } }
    }

    private data class Dab(
        val center: Offset,
        val size: Size,
        val color: Color,
        val degrees: Float,
        val alpha: Float,
    )

    private fun dabs(width: Float, height: Float, seed: Int, count: Int, scale: Float, bias: Int = 0): List<Dab> {
        val random = Random(seed)
        return List(count) {
            val color = palette[(bias + random.nextInt(palette.size)) % palette.size]
            Dab(
                center = Offset(random.nextFloat() * width, random.nextFloat() * height),
                size = Size(scale * (1.4f + random.nextFloat() * 2.2f), scale * (0.45f + random.nextFloat() * 0.5f)),
                color = color,
                degrees = random.nextFloat() * 50f - 25f,
                alpha = 0.55f + random.nextFloat() * 0.4f,
            )
        }
    }

    private fun DrawScope.drawDab(dab: Dab) {
        rotate(dab.degrees, dab.center) {
            drawOval(
                color = dab.color.copy(alpha = dab.alpha),
                topLeft = Offset(dab.center.x - dab.size.width / 2f, dab.center.y - dab.size.height / 2f),
                size = dab.size,
            )
        }
    }

    private val palette = listOf(
        Color(0xFF7FA86A),
        Color(0xFFB7D3A8),
        Color(0xFFE7B7C8),
        Color(0xFFF4D6E2),
        Color(0xFF8FB7C9),
        Color(0xFFD5E4C4),
        Color(0xFFE6D39A),
        Color(0xFF6E8F73),
        Color(0xFFC48B9A),
        Color(0xFFF7F3EA),
        Color(0xFF4F6F62),
    )
}
