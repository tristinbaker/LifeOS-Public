package com.lifeos.modules.lifeos_sports.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lifeos.modules.lifeos_sports.domain.model.League

class GameStartNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val gameId = inputData.getString(KEY_GAME_ID) ?: return Result.failure()
        val homeTeam = inputData.getString(KEY_HOME_TEAM) ?: return Result.failure()
        val awayTeam = inputData.getString(KEY_AWAY_TEAM) ?: return Result.failure()
        val leagueName = inputData.getString(KEY_LEAGUE) ?: return Result.failure()

        val emoji = when (leagueName) {
            League.MLB.name -> "⚾"
            League.NBA.name -> "🏀"
            League.NHL.name -> "🏒"
            League.NFL.name, League.NCAA_FOOTBALL.name -> "🏈"
            else -> "🏟"
        }

        createChannel()

        val tapIntent = Intent().apply {
            setClassName(applicationContext.packageName, "com.tristinbaker.lifeos.MainActivity")
            putExtra("module", "sports")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, gameId.hashCode(), tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("$emoji Game starting now")
            .setContentText("$awayTeam @ $homeTeam")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        applicationContext.getSystemService(NotificationManager::class.java)
            .notify(gameId.hashCode(), notification)

        return Result.success()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Game Start Alerts", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when your favorite teams' games start"
        }
        applicationContext.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    companion object {
        const val TAG = "game_start_notification"
        const val CHANNEL_ID = "sports_game_alerts"
        const val KEY_GAME_ID = "game_id"
        const val KEY_HOME_TEAM = "home_team"
        const val KEY_AWAY_TEAM = "away_team"
        const val KEY_LEAGUE = "league"
    }
}
