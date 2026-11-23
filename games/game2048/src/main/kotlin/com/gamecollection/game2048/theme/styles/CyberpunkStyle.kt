package com.gamecollection.game2048.theme.styles

import androidx.compose.ui.geometry.CornerRadius
import com.gamecollection.game2048.theme.CanvasSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamecollection.game2048.theme.ArtChrome
import com.gamecollection.game2048.theme.ArtStyle
import com.gamecollection.game2048.theme.shaderOrCanvas
import com.gamecollection.game2048.theme.tierOf

object CyberpunkStyle : ArtStyle() {
    override val id: String = "cyber"
    override val name: String = "赛博朋克"
    override val blurb: String = "霓虹切角、扫描线和夜城网格"
    override val fontFamily: FontFamily = FontFamily.Monospace
    override val lightStatusIcons: Boolean = true
    override val gridGap = 7.dp
    override val gridPadding = 8.dp
    override val numberLetterSpacing = 1.sp
    override val chrome = ArtChrome(
        textPrimary = Color(0xFFE8FBFF),
        textMuted = Color(0xFF8AA4B8),
        accent = Color(0xFF14F1E0),
        onAccent = Color(0xFF041018),
        panel = Color(0xFF101820),
        onPanel = Color(0xFFE8FBFF),
        outline = Color(0xFFFF2BD6),
        corner = 10.dp,
        buttonCorner = 4.dp,
    )

    override fun numberColor(value: Int): Color = neon(value)

    override fun numberShadow(value: Int): Shadow = Shadow(neon(value), Offset(0f, 0f), 12f)

    override fun prepareBackdrop(scope: CanvasSize): DrawScope.() -> Unit {
        val canvas = city(scope)
        return shaderOrCanvas(scope, gridShader, canvas)
    }

    override fun prepareBoard(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val radius = CornerRadius(18f, 18f)
        return {
            drawRoundRect(Color(0xFF070B10), cornerRadius = radius)
            drawRoundRect(Color(0xFF14F1E0), cornerRadius = radius, style = Stroke(width = 2f))
            drawRoundRect(
                Color(0xFFFF2BD6).copy(alpha = 0.8f),
                topLeft = Offset(3f, 3f),
                size = Size(size.width - 6f, size.height - 6f),
                cornerRadius = radius,
                style = Stroke(width = 1f),
            )
        }
    }

    override fun prepareEmpty(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val radius = CornerRadius(size.minDimension * 0.08f, size.minDimension * 0.08f)
        return {
            drawRoundRect(Color(0xFF101820), cornerRadius = radius)
            drawRoundRect(Color(0xFF14F1E0).copy(alpha = 0.25f), cornerRadius = radius, style = Stroke(1.5f))
        }
    }

    override fun prepareTile(scope: CanvasSize, value: Int): DrawScope.() -> Unit = with(scope) {
        val glow = neon(value)
        val radius = CornerRadius(size.minDimension * 0.1f, size.minDimension * 0.1f)
        val brackets = bracketPath(size)
        return {
            drawRoundRect(glow.copy(alpha = 0.28f), cornerRadius = radius)
            drawRoundRect(Color(0xFF07060D), cornerRadius = radius)
            val inset = size.minDimension * 0.07f
            drawRoundRect(
                Color(0xFF121826),
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                cornerRadius = radius,
            )
            drawRoundRect(glow, cornerRadius = radius, style = Stroke(width = size.minDimension * 0.035f))
            drawPath(brackets, glow, style = Stroke(width = size.minDimension * 0.025f))
        }
    }

    private fun city(scope: CanvasSize): DrawScope.() -> Unit = with(scope) {
        val step = 28f
        val lines = mutableListOf<Float>()
        var y = 0f
        while (y < size.height) {
            lines += y
            y += 4f
        }
        return {
            drawRect(Color(0xFF070A12))
            var x = 0f
            while (x < size.width) {
                drawLine(Color(0xFF123044), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var gy = 0f
            while (gy < size.height) {
                drawLine(Color(0xFF123044), Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1f)
                gy += step
            }
            drawRect(
                Color(0xFFFF2BD6).copy(alpha = 0.18f),
                topLeft = Offset(0f, size.height * 0.62f),
                size = Size(size.width, size.height * 0.38f),
            )
            drawRect(
                Color(0xFF14F1E0).copy(alpha = 0.12f),
                topLeft = Offset(0f, size.height * 0.2f),
                size = Size(size.width * 0.45f, size.height * 0.25f),
            )
            lines.forEach { scan ->
                drawLine(Color.Black.copy(alpha = 0.18f), Offset(0f, scan), Offset(size.width, scan), 1f)
            }
        }
    }

    private fun bracketPath(size: Size): Path {
        val arm = size.minDimension * 0.22f
        val inset = size.minDimension * 0.08f
        return Path().apply {
            moveTo(inset, inset + arm)
            lineTo(inset, inset)
            lineTo(inset + arm, inset)
            moveTo(size.width - inset - arm, inset)
            lineTo(size.width - inset, inset)
            lineTo(size.width - inset, inset + arm)
            moveTo(inset, size.height - inset - arm)
            lineTo(inset, size.height - inset)
            lineTo(inset + arm, size.height - inset)
            moveTo(size.width - inset, size.height - inset - arm)
            lineTo(size.width - inset, size.height - inset)
            lineTo(size.width - inset - arm, size.height - inset)
        }
    }

    private fun neon(value: Int): Color = neons[tierOf(value) % neons.size]

    private val neons = listOf(
        Color(0xFF14F1E0),
        Color(0xFFFF2BD6),
        Color(0xFFF6F25A),
        Color(0xFF7CFF6B),
        Color(0xFFFF7A3C),
        Color(0xFFB388FF),
        Color(0xFFFF4D6D),
        Color(0xFF7DEBFF),
        Color(0xFFFFD166),
        Color(0xFFFF3B6B),
        Color(0xFFF8FFF8),
    )

    private val gridShader = """
            uniform float2 iResolution;
            half4 main(float2 fragCoord) {
                float2 uv = fragCoord / iResolution;
                half3 col = half3(0.03, 0.04, 0.07);
                float gx = abs(fract(fragCoord.x / 28.0) - 0.5);
                float gy = abs(fract(fragCoord.y / 28.0) - 0.5);
                float grid = smoothstep(0.48, 0.5, max(gx, gy));
                col += half3(0.05, 0.16, 0.22) * half(grid);
                col += half3(1.0, 0.1, 0.7) * half(smoothstep(0.55, 1.0, uv.y) * 0.25);
                col += half3(0.05, 0.9, 0.85) * half(smoothstep(0.0, 0.4, 1.0 - uv.x) * 0.12);
                float scan = step(0.5, fract(fragCoord.y / 3.0));
                col *= half(0.82 + 0.18 * scan);
                return half4(col, 1.0);
            }
        """.trimIndent()
}
