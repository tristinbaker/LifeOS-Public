package com.lifeos.modules.lifeos_sports.notification

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.sportsNotifDataStore by preferencesDataStore("sports_game_notifications")

@Singleton
class GameNotificationStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY = stringSetPreferencesKey("notif_team_ids")

    val notificationTeamIds: Flow<Set<String>> =
        context.sportsNotifDataStore.data.map { it[KEY] ?: emptySet() }

    suspend fun setEnabled(compositeId: String, enabled: Boolean) {
        context.sportsNotifDataStore.edit { prefs ->
            val current = prefs[KEY] ?: emptySet()
            prefs[KEY] = if (enabled) current + compositeId else current - compositeId
        }
    }

    suspend fun getAll(): Set<String> =
        context.sportsNotifDataStore.data.map { it[KEY] ?: emptySet() }.first()
}
