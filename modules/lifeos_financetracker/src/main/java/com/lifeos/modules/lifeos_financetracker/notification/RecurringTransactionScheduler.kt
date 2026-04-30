package com.lifeos.modules.lifeos_financetracker.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class RecurringTransactionScheduler(private val context: Context) {

    fun scheduleDailyCheck() {
        val now = LocalDateTime.now()
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
        val initialDelay = nextMidnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                System.currentTimeMillis()

        val request = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay.coerceAtLeast(0), TimeUnit.MILLISECONDS)
            .addTag("recurring_finance")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "recurring_finance",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
