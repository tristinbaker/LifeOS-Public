package com.lifeos.modules.lifeos_habittracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class HabitReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val habitName = inputData.getString(KEY_HABIT_NAME) ?: return Result.success()
        val habitId = inputData.getLong(KEY_HABIT_ID, -1L)
        createNotificationChannel()
        showNotification(habitName, habitId)
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to complete your habits"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun showNotification(habitName: String, habitId: Long) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("Habit reminder")
            .setContentText("Time to: $habitName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify((NOTIFICATION_ID_BASE + habitId).toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "habit_reminder_channel"
        const val NOTIFICATION_ID_BASE = 2000
        const val KEY_HABIT_NAME = "habit_name"
        const val KEY_HABIT_ID = "habit_id"
    }
}
