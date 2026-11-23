package com.gamecollection.game2048.theme.styles

import androidx.compose.ui.geometry.CornerRadius
import com.gamecollection.game2048.theme.CanvasSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamecollection.game2048.theme.ArtChrome
import com.gamecollection.game2048.theme.ArtStyle
import com.gamecollection.game2048.theme.inkSilhouette
import com.gamecollection.game2048.theme.lerpColor
import com.gamecollection.game2048.theme.onColor
import com.gamecollection.game2048.theme.specks
import com.gamecollection.game2048.theme.tierOf
import kotlin.random.Random

object InkWashStyle : ArtStyle() {
    override val id: String = "ink"
    override val name: String = "水墨画"
    override val blurb: String = "宣纸、淡墨和一方朱印"
    override val fontFamily: FontFamily = FontFamily.Serif
    override val numberWeight: FontWeight = FontWeight.Black
    override val gridGap = 11.dp
    override val gridPadding = 12.dp
    override val chrome = ArtChrome(
        textPrimary = Color(0xFF2A241C),
        textMuted = Color(0xFF6E6256),
        accent = Color(0xFF8E2E2A),
        onAccent = Color(0xFFF8F1E6),
        panel = Color(0xFFF4EFE4),
        onPanel = Color(0xFF2A241C),
        outline = Color(0xFF2A241C),
        corner = 16.dp,
        buttonCorner = 22.dp,
    )

    override fun numberColor(value: Int): Color = onColor(wash(value))

    override fun prepareBackdrop(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val paper = Color(0xFFF3EEE4)
        val fiber = specks(size.width, size.height, 11, 80, Color(0xFF8A8175).copy(alpha = 0.18f), 1.6f)
        val blots = specks(size.width, size.height, 29, 7, Color(0xFF1C1A17).copy(alpha = 0.16f), size.minDimension * 0.18f)
        val seal = Size(size.minDimension * 0.08f, size.minDimension * 0.08f)
        return {
            drawRect(paper)
            fiber.forEach { drawCircle(it.color, it.radius, it.center) }
            blots.forEach { blot ->
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(blot.color, Color.Transparent),
                        center = blot.center,
                        radius = blot.radius,
                    ),
                    radius = blot.radius,
                    center = blot.center,
                )
            }
            val left = size.width * 0.78f
            val top = size.height * 0.08f
            drawRoundRect(
                Color(0xFFB4332C),
                topLeft = Offset(left, top),
                size = seal,
                cornerRadius = CornerRadius(4f, 4f),
            )
            drawRoundRect(
                Color(0xFFF6EFE3),
                topLeft = Offset(left + seal.width * 0.16f, top + seal.height * 0.16f),
                size = Size(seal.width * 0.68f, seal.height * 0.68f),
                cornerRadius = CornerRadius(2f, 2f),
                style = Stroke(width = seal.minDimension * 0.08f),
            )
        }
    }

    override fun prepareBoard(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val frame = inkSilhouette(size, 3)
        return {
            drawRect(Color(0xFFE7E0D2))
            drawPath(frame, Color(0xFF2C2824), style = Stroke(width = size.minDimension * 0.012f))
        }
    }

    override fun prepareEmpty(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val radius = size.minDimension * 0.28f
        return {
            drawCircle(
                Color(0xFF2A241C).copy(alpha = 0.06f),
                radius = radius,
                center = center,
            )
        }
    }

    override fun prepareTile(scope: CanvasSize, value: Int): DrawScope.() -> Unit = with(scope) {
        val color = wash(value)
        val random = Random(value * 17 + 3)
        val silhouette = inkSilhouette(size, value * 13 + 5)
        val blooms = List(4 + tierOf(value).coerceAtMost(6)) {
            val center = Offset(
                size.width * (0.2f + random.nextFloat() * 0.6f),
                size.height * (0.2f + random.nextFloat() * 0.6f),
            )
            val radius = size.minDimension * (0.18f + random.nextFloat() * 0.36f)
            center to radius
        }
        val seal = tierOf(value) >= 8
        return {
            blooms.forEach { (bloomCenter, radius) ->
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0.05f), Color.Transparent),
                        center = bloomCenter,
                        radius = radius,
                    ),
                    radius = radius,
                    center = bloomCenter,
                )
            }
            drawPath(silhouette, color.copy(alpha = 0.78f))
            drawPath(silhouette, color.copy(alpha = 0.9f), style = Stroke(width = size.minDimension * 0.02f))
            if (seal) {
                val side = size.minDimension * 0.18f
                drawRoundRect(
                    Color(0xFFB4332C),
                    topLeft = Offset(size.width * 0.72f, size.height * 0.08f),
                    size = Size(side, side),
                    cornerRadius = CornerRadius(2f, 2f),
                )
            }
        }
    }

    private fun wash(value: Int): Color {
        val t = (tierOf(value) / 10f).coerceIn(0.08f, 1f)
        return lerpColor(Color(0xFFD5D0C6), Color(0xFF141210), t)
    }
}
