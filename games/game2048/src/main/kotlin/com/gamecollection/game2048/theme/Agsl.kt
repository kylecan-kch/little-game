package com.gamecollection.game2048.theme

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * API 33+ shader backdrop. The canvas painting is always prepared as well and used
 * when the runtime shader is missing or fails to compile.
 */
internal fun shaderOrCanvas(
    scope: CanvasSize,
    source: String,
    canvas: DrawScope.() -> Unit,
): DrawScope.() -> Unit {
    if (Build.VERSION.SDK_INT < 33 || scope.size.minDimension <= 0f) return canvas
    val shader = try {
        compileShader(source, scope.size.width, scope.size.height)
    } catch (_: Throwable) {
        return canvas
    }
    val brush = ShaderBrush(shader)
    var enabled = true
    return draw@{
        if (enabled) {
            try {
                drawRect(brush)
                return@draw
            } catch (_: Throwable) {
                enabled = false
            }
        }
        canvas()
    }
}

@RequiresApi(33)
private fun compileShader(source: String, width: Float, height: Float): RuntimeShader =
    RuntimeShader(source).apply {
        setFloatUniform("iResolution", width, height)
    }
