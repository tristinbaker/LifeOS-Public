package com.lifeos.modules.lifeos_sports.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameAlertScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Enqueue a daily periodic worker at 7 AM. Safe to call multiple times (KEEP policy).
    fun scheduleDailyCheck() {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelayMs = target.timeInMillis - now.timeInMillis

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "game_alert_daily",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<GameAlertWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .addTag(GameAlertWorker.TAG)
                .build()
        )
    }

    // Run immediately to pick up today's games after a notification preference changes.
    fun scheduleNow() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            "game_alert_now",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<GameAlertWorker>()
                .addTag(GameAlertWorker.TAG)
                .build()
        )
    }
}
