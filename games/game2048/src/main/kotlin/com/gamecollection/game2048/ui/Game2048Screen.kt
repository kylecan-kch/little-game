package com.gamecollection.game2048.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.gamecollection.engine2048.Direction
import com.gamecollection.engine2048.Status
import com.gamecollection.game2048.Game2048UiState
import com.gamecollection.game2048.R
import com.gamecollection.game2048.theme.ArtChrome
import com.gamecollection.game2048.theme.ArtStyle
import com.gamecollection.game2048.theme.ArtStyles
import androidx.compose.runtime.LaunchedEffect

@Composable
fun Game2048Screen(
    state: Game2048UiState,
    onSwipe: (Direction) -> Unit,
    onUndo: () -> Unit,
    onNewGame: () -> Unit,
    onConfirmNewGame: () -> Unit,
    onDismissNewGame: () -> Unit,
    onContinue: () -> Unit,
    onOpenThemes: () -> Unit,
    onBack: () -> Unit,
) {
    val style = ArtStyles.byId(state.themeId)
    StatusBarIcons(lightIcons = style.lightStatusIcons)
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(state.swipeSerial) {
        if (state.swipeSerial > 0L) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    Box(Modifier.fillMaxSize()) {
        ArtBackdrop(style, Modifier.fillMaxSize())
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.loading),
                    color = style.chrome.textPrimary,
                    fontFamily = style.fontFamily,
                    fontSize = 18.sp,
                )
            }
        } else {
            Playfield(
                style = style,
                state = state,
                onSwipe = onSwipe,
                onUndo = onUndo,
                onNewGame = onNewGame,
                onOpenThemes = onOpenThemes,
                onBack = onBack,
            )
        }
        when {
            state.confirmNewGame -> ArtDialog(
                chrome = style.chrome,
                fontFamily = style.fontFamily,
                title = stringResource(R.string.new_game_title),
                message = stringResource(R.string.new_game_message),
                primary = stringResource(R.string.confirm),
                onPrimary = onConfirmNewGame,
                secondary = stringResource(R.string.cancel),
                onSecondary = onDismissNewGame,
            )
            state.status == Status.Won -> ArtDialog(
                chrome = style.chrome,
                fontFamily = style.fontFamily,
                title = stringResource(R.string.win_title),
                message = stringResource(R.string.win_message),
                primary = stringResource(R.string.keep_playing),
                onPrimary = onContinue,
                secondary = stringResource(R.string.play_again),
                onSecondary = onConfirmNewGame,
            )
            state.status == Status.Lost -> ArtDialog(
                chrome = style.chrome,
                fontFamily = style.fontFamily,
                title = stringResource(R.string.lose_title),
                message = stringResource(R.string.lose_message, state.score),
                primary = stringResource(R.string.play_again),
                onPrimary = onConfirmNewGame,
                secondary = stringResource(R.string.undo).takeIf { state.canUndo },
                onSecondary = onUndo.takeIf { state.canUndo },
            )
        }
    }
}

@Composable
private fun Playfield(
    style: ArtStyle,
    state: Game2048UiState,
    onSwipe: (Direction) -> Unit,
    onUndo: () -> Unit,
    onNewGame: () -> Unit,
    onOpenThemes: () -> Unit,
    onBack: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val landscape = maxWidth > maxHeight
        if (landscape) {
            Row(
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Header(style, onBack, onOpenThemes)
                    ScoreRow(style, state.score, state.best)
                    ActionRow(style, state, onNewGame, onUndo)
                    Hint(style)
                }
                Spacer(Modifier.size(12.dp))
                Box(
                    Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    Board(style, state, onSwipe, Modifier.fillMaxSize())
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Header(style, onBack, onOpenThemes)
                Spacer(Modifier.size(10.dp))
                ScoreRow(style, state.score, state.best)
                Spacer(Modifier.size(10.dp))
                ActionRow(style, state, onNewGame, onUndo)
                Spacer(Modifier.size(10.dp))
                Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
                    Board(style, state, onSwipe, Modifier.fillMaxSize())
                }
                Spacer(Modifier.size(8.dp))
                Hint(style)
            }
        }
    }
}

@Composable
private fun Board(
    style: ArtStyle,
    state: Game2048UiState,
    onSwipe: (Direction) -> Unit,
    modifier: Modifier,
) {
    GameBoard(
        style = style,
        tiles = state.tiles,
        ghosts = state.ghosts,
        enabled = state.status != Status.Won && state.status != Status.Lost,
        boardDescription = stringResource(R.string.cd_board),
        onSwipe = onSwipe,
        modifier = modifier,
    )
}

@Composable
private fun ActionRow(
    style: ArtStyle,
    state: Game2048UiState,
    onNewGame: () -> Unit,
    onUndo: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ArtButton(
            label = stringResource(R.string.new_game),
            chrome = style.chrome,
            fontFamily = style.fontFamily,
            filled = true,
            enabled = !state.busy,
            onClick = onNewGame,
            modifier = Modifier.weight(1f),
        )
        ArtButton(
            label = stringResource(R.string.undo),
            chrome = style.chrome,
            fontFamily = style.fontFamily,
            filled = false,
            enabled = state.canUndo && !state.busy,
            onClick = onUndo,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Hint(style: ArtStyle) {
    Text(
        text = stringResource(R.string.hint),
        color = style.chrome.textMuted,
        fontFamily = style.fontFamily,
        fontSize = with(LocalDensity.current) { 13.dp.toSp() },
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
    )
}

@Composable
private fun Header(style: ArtStyle, onBack: () -> Unit, onOpenThemes: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = style.chrome.textPrimary,
            )
        }
        Text(
            text = "2048",
            color = style.chrome.textPrimary,
            fontFamily = style.fontFamily,
            fontWeight = FontWeight.Black,
            fontSize = with(LocalDensity.current) { 28.dp.toSp() },
            modifier = Modifier.weight(1f),
        )
        ArtButton(
            label = stringResource(R.string.themes),
            chrome = style.chrome,
            fontFamily = style.fontFamily,
            filled = false,
            enabled = true,
            onClick = onOpenThemes,
        )
    }
}

@Composable
private fun ScoreRow(style: ArtStyle, score: Int, best: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ScoreCard(style, stringResource(R.string.score_current), score, Modifier.weight(1f))
        ScoreCard(style, stringResource(R.string.score_best), best, Modifier.weight(1f))
    }
}

@Composable
private fun ScoreCard(style: ArtStyle, label: String, value: Int, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(style.chrome.corner))
            .background(style.chrome.panel)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = style.chrome.textMuted,
            fontFamily = style.fontFamily,
            fontSize = with(LocalDensity.current) { 12.dp.toSp() },
            maxLines = 1,
            softWrap = false,
        )
        FittedScore(
            text = value.toString(),
            color = style.chrome.onPanel,
            fontFamily = style.fontFamily,
            fontWeight = FontWeight.Black,
            maxSize = 22.dp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FittedScore(
    text: String,
    color: Color,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    maxSize: Dp,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val measurer = rememberTextMeasurer()
        val maxWidthPx = constraints.maxWidth
        val fontSize = remember(text, maxWidthPx, fontFamily, fontWeight, maxSize, density.density, density.fontScale) {
            val maxDp = maxSize.value
            val minDp = 10f
            fun fits(sizeDp: Float): Boolean {
                if (maxWidthPx == Constraints.Infinity) return true
                if (maxWidthPx <= 0) return false
                val width = measurer.measure(
                    text = AnnotatedString(text),
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        fontSize = with(density) { sizeDp.dp.toSp() },
                    ),
                    maxLines = 1,
                    softWrap = false,
                ).size.width
                return width <= maxWidthPx
            }
            val chosen = if (fits(maxDp)) {
                maxDp
            } else {
                var low = minDp
                var high = maxDp
                repeat(12) {
                    val mid = (low + high) / 2f
                    if (fits(mid)) low = mid else high = mid
                }
                low
            }
            with(density) { chosen.dp.toSp() }
        }
        Text(
            text = text,
            color = color,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            fontSize = fontSize,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun ArtButton(
    label: String,
    chrome: ArtChrome,
    fontFamily: FontFamily,
    filled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (filled) chrome.accent else Color.Transparent
    val foreground = if (filled) chrome.onAccent else chrome.accent
    val shape = RoundedCornerShape(chrome.buttonCorner)
    Box(
        modifier
            .clip(shape)
            .border(
                width = if (filled) 0.dp else 1.5.dp,
                color = if (enabled) chrome.outline else chrome.outline.copy(alpha = 0.4f),
                shape = shape,
            )
            .background(if (enabled) background else background.copy(alpha = 0.35f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (enabled) foreground else foreground.copy(alpha = 0.45f),
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = with(LocalDensity.current) { 15.dp.toSp() },
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun ArtDialog(
    chrome: ArtChrome,
    fontFamily: FontFamily,
    title: String,
    message: String,
    primary: String,
    onPrimary: () -> Unit,
    secondary: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(chrome.scrim)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {},
                ),
        )
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp),
        ) {
            Column(
                Modifier
                    .align(Alignment.Center)
                    .widthIn(max = minOf(maxWidth, 420.dp))
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
                    .clip(RoundedCornerShape(chrome.corner))
                    .background(chrome.panel)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    color = chrome.onPanel,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = with(LocalDensity.current) { 22.dp.toSp() },
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = message,
                    color = chrome.onPanel.copy(alpha = 0.84f),
                    fontFamily = fontFamily,
                    fontSize = with(LocalDensity.current) { 15.dp.toSp() },
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.size(16.dp))
                ArtButton(
                    label = primary,
                    chrome = chrome,
                    fontFamily = fontFamily,
                    filled = true,
                    enabled = true,
                    onClick = onPrimary,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (secondary != null && onSecondary != null) {
                    Spacer(Modifier.size(8.dp))
                    ArtButton(
                        label = secondary,
                        chrome = chrome,
                        fontFamily = fontFamily,
                        filled = false,
                        enabled = true,
                        onClick = onSecondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBarIcons(lightIcons: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !lightIcons
        controller.isAppearanceLightNavigationBars = !lightIcons
    }
}
