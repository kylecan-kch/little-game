package com.gamecollection.arcade

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.gamecollection.game2048.data.Game2048Repository

val Context.gameCollectionStore by preferencesDataStore(name = "game_collection")

class GamesApplication : Application() {
    val repository: Game2048Repository by lazy {
        Game2048Repository(gameCollectionStore)
    }
}
