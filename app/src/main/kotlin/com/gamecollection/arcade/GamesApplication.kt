package com.gamecollection.arcade

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.gamecollection.game2048.data.Game2048Repository

private const val TAG = "GamesApplication"

val Context.gameCollectionStore by preferencesDataStore(
    name = "game_collection",
    corruptionHandler = ReplaceFileCorruptionHandler { exception ->
        Log.e(TAG, "Replacing corrupt game preferences", exception)
        emptyPreferences()
    },
)

class GamesApplication : Application() {
    val repository: Game2048Repository by lazy {
        Game2048Repository(gameCollectionStore)
    }
}
