package com.gamecollection.game2048.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

internal fun tierOf(value: Int): Int {
    var tier = 0
    var current = value
    while (current > 2) {
        current /= 2
        tier++
    }
    return tier
}

internal fun lerpColor(start: Color, end: Color, amount: Float): Color {
    val t = amount.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * t,
        green = start.green + (end.green - start.green) * t,
        blue = start.blue + (end.blue - start.blue) * t,
        alpha = start.alpha + (end.alpha - start.alpha) * t,
    )
}

internal fun onColor(background: Color): Color {
    val luminance = 0.299f * background.red + 0.587f * background.green + 0.114f * background.blue
    return if (luminance > 0.62f) Color(0xFF241C16) else Color(0xFFF8F4EC)
}

internal data class Speck(val center: Offset, val radius: Float, val color: Color)

internal fun specks(
    width: Float,
    height: Float,
    seed: Int,
    count: Int,
    color: Color,
    maxRadius: Float,
): List<Speck> {
    val random = Random(seed)
    return List(count) {
        Speck(
            center = Offset(random.nextFloat() * width, random.nextFloat() * height),
            radius = maxRadius * (0.25f + random.nextFloat()),
            color = color.copy(alpha = (color.alpha * (0.3f + random.nextFloat() * 0.7f)).coerceIn(0f, 1f)),
        )
    }
}

internal data class StrokeCurve(
    val path: Path,
    val color: Color,
    val width: Float,
)

internal fun impastoCurves(
    width: Float,
    height: Float,
    seed: Int,
    count: Int,
    palette: List<Color>,
    minLength: Float,
    maxLength: Float,
    widthFactor: Float,
): List<StrokeCurve> {
    val random = Random(seed)
    return List(count) { index ->
        val start = Offset(random.nextFloat() * width, random.nextFloat() * height)
        val angle = random.nextFloat() * 6.28318f
        val length = minLength + random.nextFloat() * (maxLength - minLength)
        val bend = (random.nextFloat() - 0.4f) * 1.4f
        val control = Offset(
            start.x + cos(angle) * length * 0.55f + cos(angle + bend) * length * 0.35f,
            start.y + sin(angle) * length * 0.55f + sin(angle + bend) * length * 0.35f,
        )
        val end = Offset(
            start.x + cos(angle + bend) * length,
            start.y + sin(angle + bend) * length,
        )
        val path = Path().apply {
            moveTo(start.x, start.y)
            quadraticTo(control.x, control.y, end.x, end.y)
        }
        StrokeCurve(
            path = path,
            color = palette[index % palette.size].copy(alpha = 0.55f + random.nextFloat() * 0.4f),
            width = widthFactor * (0.7f + random.nextFloat() * 0.8f),
        )
    }
}

internal fun wavePath(width: Float, y: Float, amplitude: Float, phase: Float): Path = Path().apply {
    moveTo(0f, y)
    quadraticTo(width * 0.25f, y - amplitude + phase, width * 0.5f, y + phase * 0.2f)
    quadraticTo(width * 0.78f, y + amplitude, width, y - phase)
}

internal fun inkSilhouette(size: Size, seed: Int): Path {
    val random = Random(seed)
    val width = size.width
    val height = size.height
    fun jitter(base: Float, amount: Float) = base + (random.nextFloat() - 0.5f) * 2f * amount
    return Path().apply {
        moveTo(jitter(width * 0.22f, width * 0.04f), jitter(height * 0.08f, height * 0.03f))
        cubicTo(
            jitter(width * 0.55f, width * 0.05f),
            jitter(height * 0.0f, height * 0.03f),
            jitter(width * 0.9f, width * 0.04f),
            jitter(height * 0.12f, height * 0.04f),
            jitter(width * 0.94f, width * 0.03f),
            jitter(height * 0.38f, height * 0.05f),
        )
        cubicTo(
            jitter(width * 1.02f, width * 0.02f),
            jitter(height * 0.58f, height * 0.04f),
            jitter(width * 0.88f, width * 0.04f),
            jitter(height * 0.82f, height * 0.04f),
            jitter(width * 0.62f, width * 0.05f),
            jitter(height * 0.94f, height * 0.03f),
        )
        cubicTo(
            jitter(width * 0.36f, width * 0.04f),
            jitter(height * 1.04f, height * 0.02f),
            jitter(width * 0.12f, width * 0.04f),
            jitter(height * 0.86f, height * 0.04f),
            jitter(width * 0.06f, width * 0.03f),
            jitter(height * 0.6f, height * 0.05f),
        )
        cubicTo(
            jitter(width * -0.02f, width * 0.02f),
            jitter(height * 0.36f, height * 0.04f),
            jitter(width * 0.08f, width * 0.03f),
            jitter(height * 0.16f, height * 0.03f),
            jitter(width * 0.22f, width * 0.04f),
            jitter(height * 0.08f, height * 0.03f),
        )
        close()
    }
}
