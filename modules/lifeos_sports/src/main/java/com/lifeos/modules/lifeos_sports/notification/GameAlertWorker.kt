package com.lifeos.modules.lifeos_sports.notification

import android.content.Context
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.lifeos.modules.lifeos_sports.data.api.EspnApiService
import com.lifeos.modules.lifeos_sports.domain.model.League
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.concurrent.TimeUnit

class GameAlertWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notifIds = applicationContext.sportsNotifDataStore.data
            .map { it[stringSetPreferencesKey("notif_team_ids")] ?: emptySet() }
            .first()

        if (notifIds.isEmpty()) return Result.success()

        val byLeague = notifIds.mapNotNull { id ->
            val underscore = id.indexOf('_')
            if (underscore < 1) return@mapNotNull null
            val leagueName = id.substring(0, underscore)
            val teamId = id.substring(underscore + 1)
            try { League.valueOf(leagueName) to teamId } catch (_: Exception) { null }
        }.groupBy({ it.first }, { it.second })

        val now = Instant.now()

        for ((league, teamIds) in byLeague) {
            val response = EspnApiService.getScoreboard(league) ?: continue
            for (event in response.events) {
                if (event.date.isBlank()) continue
                val comp = event.competitions.firstOrNull() ?: continue
                if (comp.status.type.state != "pre") continue

                val home = comp.competitors.firstOrNull { it.homeAway == "home" } ?: continue
                val away = comp.competitors.firstOrNull { it.homeAway == "away" } ?: continue

                if (teamIds.none { it == home.team.id || it == away.team.id }) continue

                val gameStart = try { Instant.parse(event.date) } catch (_: Exception) { continue }
                val delayMs = gameStart.toEpochMilli() - now.toEpochMilli()
                if (delayMs < -60_000) continue // game already started more than a minute ago

                val data = workDataOf(
                    GameStartNotificationWorker.KEY_GAME_ID to event.id,
                    GameStartNotificationWorker.KEY_HOME_TEAM to home.team.displayName,
                    GameStartNotificationWorker.KEY_AWAY_TEAM to away.team.displayName,
                    GameStartNotificationWorker.KEY_LEAGUE to league.name
                )

                WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                    "game_start_${event.id}",
                    ExistingWorkPolicy.KEEP,
                    OneTimeWorkRequestBuilder<GameStartNotificationWorker>()
                        .setInitialDelay(delayMs.coerceAtLeast(0L), TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .build()
                )
            }
        }

        return Result.success()
    }

    companion object {
        const val TAG = "GameAlertWorker"
    }
}
