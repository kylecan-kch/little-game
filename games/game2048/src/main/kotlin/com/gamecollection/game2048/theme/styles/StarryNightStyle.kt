package com.gamecollection.game2048.theme.styles

import androidx.compose.ui.geometry.CornerRadius
import com.gamecollection.game2048.theme.CanvasSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamecollection.game2048.theme.ArtChrome
import com.gamecollection.game2048.theme.ArtStyle
import com.gamecollection.game2048.theme.impastoCurves
import com.gamecollection.game2048.theme.shaderOrCanvas
import com.gamecollection.game2048.theme.specks
import com.gamecollection.game2048.theme.tierOf
import kotlin.random.Random

object StarryNightStyle : ArtStyle() {
    override val id: String = "starry"
    override val name: String = "梵高星空"
    override val blurb: String = "厚涂旋涡、月色和星点"
    override val fontFamily: FontFamily = FontFamily.Serif
    override val numberWeight: FontWeight = FontWeight.Black
    override val lightStatusIcons: Boolean = true
    override val gridGap = 8.dp
    override val chrome = ArtChrome(
        textPrimary = Color(0xFFF6E7B2),
        textMuted = Color(0xFFB7C4DE),
        accent = Color(0xFFF0C94A),
        onAccent = Color(0xFF142044),
        panel = Color(0xFF15244A),
        onPanel = Color(0xFFF6E7B2),
        outline = Color(0xFF7FD0E8),
        corner = 18.dp,
        buttonCorner = 18.dp,
    )

    override fun numberColor(value: Int): Color = Color(0xFFFFF4C8)

    override fun numberShadow(value: Int): Shadow = Shadow(
        color = Color(0xFF0B1C4A),
        offset = Offset(0f, 2f),
        blurRadius = 6f,
    )

    override fun prepareBackdrop(scope: CanvasSize): DrawScope.() -> Unit {
        val canvas = starryField(scope, 70, 4)
        return shaderOrCanvas(scope, skyShader, canvas)
    }

    override fun prepareBoard(scope: CanvasSize): DrawScope.() -> Unit = starryField(scope, 28, 9)

    override fun prepareEmpty(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val radius = CornerRadius(size.minDimension * 0.18f, size.minDimension * 0.18f)
        return {
            drawRoundRect(Color(0xFF0E1A38).copy(alpha = 0.72f), cornerRadius = radius)
        }
    }

    override fun prepareTile(scope: CanvasSize, value: Int): DrawScope.() -> Unit = with(scope) {
        val palette = tilePalette(tierOf(value))
        val curves = impastoCurves(
            width = size.width,
            height = size.height,
            seed = value * 19 + 4,
            count = 14,
            palette = palette,
            minLength = size.minDimension * 0.25f,
            maxLength = size.minDimension * 0.7f,
            widthFactor = size.minDimension * 0.07f,
        )
        val stars = specks(size.width, size.height, value * 3, 3 + tierOf(value) / 3, Color(0xFFFFF6D0), size.minDimension * 0.05f)
        val radius = CornerRadius(size.minDimension * 0.16f, size.minDimension * 0.16f)
        return {
            drawRoundRect(Color(0xFF10224C), cornerRadius = radius)
            curves.forEach { curve ->
                drawPath(curve.path, curve.color, style = Stroke(width = curve.width, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
            stars.forEach { star ->
                drawCircle(star.color, star.radius, star.center)
                drawCircle(Color(0xFFFFF6D0).copy(alpha = 0.35f), star.radius * 2.4f, star.center)
            }
        }
    }

    private fun starryField(scope: CanvasSize, count: Int, seed: Int): DrawScope.() -> Unit = with(scope) {
        val curves = impastoCurves(
            width = size.width,
            height = size.height,
            seed = seed,
            count = count,
            palette = listOf(Color(0xFF1D3E86), Color(0xFF2E62B0), Color(0xFFF2D15C), Color(0xFF8FDBEF), Color(0xFFF7F1DE)),
            minLength = size.minDimension * 0.12f,
            maxLength = size.minDimension * 0.34f,
            widthFactor = size.minDimension * 0.018f,
        )
        val stars = specks(size.width, size.height, seed + 20, count / 2, Color(0xFFFFF8DE), size.minDimension * 0.008f)
        val cypress = cypressPath(size.width, size.height)
        return {
            drawRect(Color(0xFF0C1733))
            curves.forEach { curve ->
                drawPath(curve.path, curve.color, style = Stroke(width = curve.width, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
            drawPath(cypress, Color(0xFF071018))
            stars.forEach { drawCircle(it.color, it.radius.coerceAtLeast(1.2f), it.center) }
        }
    }

    private fun cypressPath(width: Float, height: Float) = androidx.compose.ui.graphics.Path().apply {
        val left = width * 0.78f
        moveTo(left, height)
        quadraticTo(left - width * 0.04f, height * 0.62f, left + width * 0.02f, height * 0.28f)
        quadraticTo(left + width * 0.05f, height * 0.08f, left + width * 0.01f, height * 0.02f)
        quadraticTo(left + width * 0.08f, height * 0.2f, left + width * 0.06f, height * 0.55f)
        quadraticTo(left + width * 0.1f, height * 0.78f, left + width * 0.05f, height)
        close()
    }

    private fun tilePalette(tier: Int): List<Color> = when (tier % 4) {
        0 -> listOf(Color(0xFFF6E27A), Color(0xFF8FD4EA), Color(0xFFF7F3E3))
        1 -> listOf(Color(0xFF7FD0E8), Color(0xFFF2C94C), Color(0xFFD7E7F5))
        2 -> listOf(Color(0xFFE7F6FF), Color(0xFFF0B429), Color(0xFF9EC8F0))
        else -> listOf(Color(0xFFFFF1B8), Color(0xFF5E8ED6), Color(0xFFFFFFFF))
    }

    private val skyShader = """
            uniform float2 iResolution;
            half4 main(float2 fragCoord) {
                float2 uv = fragCoord / iResolution;
                float2 p = uv - float2(0.42, 0.48);
                float r = length(p);
                float a = atan(p.y, p.x);
                float wave = sin(a * 7.0 - r * 22.0);
                half3 deep = half3(0.04, 0.07, 0.18);
                half3 mid = half3(0.12, 0.28, 0.58);
                half3 gold = half3(0.96, 0.82, 0.32);
                half3 col = mix(deep, mid, half(smoothstep(0.0, 1.0, uv.y)));
                col = mix(col, gold, half(smoothstep(0.35, 1.0, wave) * 0.55));
                float2 cell = floor(uv * float2(48.0, 80.0));
                float spark = fract(sin(dot(cell, float2(12.9898, 78.233))) * 43758.5453);
                col += half3(step(0.992, spark));
                return half4(col, 1.0);
            }
        """.trimIndent()
}
