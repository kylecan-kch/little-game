package com.gamecollection.arcade.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF9C3B2E),
    onPrimary = Color(0xFFFFF8F2),
    secondary = Color(0xFF2F5D50),
    onSecondary = Color(0xFFF4FFF8),
    background = Color(0xFFF3EEE4),
    onBackground = Color(0xFF2A241C),
    surface = Color(0xFFFFF9F0),
    onSurface = Color(0xFF2A241C),
    onSurfaceVariant = Color(0xFF6B5E52),
    outline = Color(0xFFD9CBB8),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE6B15C),
    onPrimary = Color(0xFF1A140C),
    secondary = Color(0xFF8FB9A6),
    onSecondary = Color(0xFF10211B),
    background = Color(0xFF12100E),
    onBackground = Color(0xFFF3EADF),
    surface = Color(0xFF1E1A16),
    onSurface = Color(0xFFF3EADF),
    onSurfaceVariant = Color(0xFFCDBFAE),
    outline = Color(0xFF4A4036),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
