package com.gamecollection.arcade.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamecollection.game2048.Game2048ViewModel
import com.gamecollection.game2048.theme.ArtStyles
import com.gamecollection.game2048.ui.Game2048Screen
import com.gamecollection.game2048.ui.ThemePickerScreen

private enum class Destination {
    Home,
    Play,
    Themes,
}

@Composable
fun AppRoot(viewModel: Game2048ViewModel) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    var destinationName by rememberSaveable { mutableStateOf(Destination.Home.name) }
    val destination = Destination.valueOf(destinationName)
    when (destination) {
        Destination.Home -> HomeScreen(
            style = ArtStyles.byId(state.themeId),
            onPlay = { destinationName = Destination.Play.name },
        )
        Destination.Play -> {
            BackHandler {
                if (state.confirmNewGame) {
                    viewModel.dismissNewGame()
                } else {
                    destinationName = Destination.Home.name
                }
            }
            Game2048Screen(
                state = state,
                onSwipe = viewModel::swipe,
                onUndo = viewModel::undo,
                onNewGame = viewModel::requestNewGame,
                onConfirmNewGame = viewModel::confirmNewGame,
                onDismissNewGame = viewModel::dismissNewGame,
                onContinue = viewModel::continueAfterWin,
                onOpenThemes = { destinationName = Destination.Themes.name },
                onBack = { destinationName = Destination.Home.name },
                onLeave = viewModel::onLeaveGame,
            )
        }
        Destination.Themes -> {
            BackHandler { destinationName = Destination.Play.name }
            ThemePickerScreen(
                selectedId = state.themeId,
                onSelect = viewModel::setTheme,
                onBack = { destinationName = Destination.Play.name },
            )
        }
    }
}
