package com.gamecollection.game2048.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gamecollection.engine2048.GameState
import com.gamecollection.engine2048.GameStateCodec
import kotlinx.coroutines.flow.first

class Game2048Repository(
    private val dataStore: DataStore<Preferences>,
) {
    data class Persisted(
        val state: GameState?,
        val best: Int,
        val themeId: String?,
    )

    suspend fun load(): Persisted {
        val prefs = dataStore.data.first()
        return Persisted(
            state = prefs[KEY_STATE]?.let(GameStateCodec::decode),
            best = (prefs[KEY_BEST] ?: 0).coerceAtLeast(0),
            themeId = prefs[KEY_THEME],
        )
    }

    suspend fun save(state: GameState, best: Int, themeId: String) {
        dataStore.edit { prefs ->
            prefs[KEY_STATE] = GameStateCodec.encode(state)
            prefs[KEY_BEST] = best.coerceAtLeast(0)
            prefs[KEY_THEME] = themeId
        }
    }

    private companion object {
        val KEY_STATE = stringPreferencesKey("state")
        val KEY_BEST = intPreferencesKey("best")
        val KEY_THEME = stringPreferencesKey("theme")
    }
}
