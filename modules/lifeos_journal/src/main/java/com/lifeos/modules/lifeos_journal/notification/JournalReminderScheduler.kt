package com.lifeos.modules.lifeos_journal.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

const val JOURNAL_REMINDER_WORK_TAG = "journal_reminder"

@Singleton
class JournalReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleReminder(reminderTimeMillis: Long) {
        val workManager = WorkManager.getInstance(context)
        
        val delay = calculateDelayUntil(reminderTimeMillis)
        
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<JournalReminderWorker>(
            1, java.util.concurrent.TimeUnit.DAYS
        )
            .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .addTag(JOURNAL_REMINDER_WORK_TAG)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            JOURNAL_REMINDER_WORK_TAG,
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelReminder() {
        WorkManager.getInstance(context).cancelAllWorkByTag(JOURNAL_REMINDER_WORK_TAG)
    }

    private fun calculateDelayUntil(targetTimeMillis: Long): Long {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, (targetTimeMillis / (60 * 60 * 1000)).toInt())
            set(java.util.Calendar.MINUTE, ((targetTimeMillis / (60 * 1000)) % 60).toInt())
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        if (target.before(now)) {
            target.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}

class JournalReminderWorker(
    private val context: Context,
    workerParams: androidx.work.WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        createNotificationChannel()
        showNotification()
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Journal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminder to write in your journal"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("Time to journal")
            .setContentText("Take a moment to write about your day")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "journal_reminder_channel"
        const val NOTIFICATION_ID = 1002
    }
}