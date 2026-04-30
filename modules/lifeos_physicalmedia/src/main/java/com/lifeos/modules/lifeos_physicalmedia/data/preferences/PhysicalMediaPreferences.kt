package com.lifeos.modules.lifeos_physicalmedia.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lifeos.modules.lifeos_physicalmedia.data.local.GameSystem
import com.lifeos.modules.lifeos_physicalmedia.data.local.MovieFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.physicalMediaPrefsDataStore by preferencesDataStore("physical_media_prefs")

@Singleton
class PhysicalMediaPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_LAST_GAME_SYSTEM = stringPreferencesKey("last_game_system")
    private val KEY_LAST_MOVIE_FORMAT = stringPreferencesKey("last_movie_format")

    val lastGameSystem: Flow<GameSystem> = context.physicalMediaPrefsDataStore.data.map { prefs ->
        prefs[KEY_LAST_GAME_SYSTEM]?.let { runCatching { GameSystem.valueOf(it) }.getOrNull() }
            ?: GameSystem.SWITCH
    }

    suspend fun setLastGameSystem(system: GameSystem) {
        context.physicalMediaPrefsDataStore.edit { it[KEY_LAST_GAME_SYSTEM] = system.name }
    }

    val lastMovieFormat: Flow<MovieFormat> = context.physicalMediaPrefsDataStore.data.map { prefs ->
        prefs[KEY_LAST_MOVIE_FORMAT]?.let { runCatching { MovieFormat.valueOf(it) }.getOrNull() }
            ?: MovieFormat.BLU_RAY
    }

    suspend fun setLastMovieFormat(format: MovieFormat) {
        context.physicalMediaPrefsDataStore.edit { it[KEY_LAST_MOVIE_FORMAT] = format.name }
    }
}
