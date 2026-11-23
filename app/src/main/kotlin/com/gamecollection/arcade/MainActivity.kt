package com.gamecollection.arcade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamecollection.arcade.ui.AppRoot
import com.gamecollection.arcade.ui.AppTheme
import com.gamecollection.game2048.Game2048ViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as GamesApplication).repository
        setContent {
            val viewModel: Game2048ViewModel = viewModel(factory = Game2048ViewModel.factory(repository))
            AppTheme {
                AppRoot(viewModel)
            }
        }
    }
}
