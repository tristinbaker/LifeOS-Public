package com.lifeos.modules.lifeos_mealtracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import com.lifeos.modules.lifeos_mealtracker.R

object NotificationHelper {
    const val CHANNEL_ID = "brutal_shame_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Shame Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminds you that you went over your calorie goal"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun getLaunchIntent(context: Context): Intent {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        return launchIntent ?: Intent().apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("module", "mealtracker")
        }
    }

    fun showShameNotification(context: Context, calorieExcess: Int) {
        val intent = getLaunchIntent(context).apply {
            putExtra("show_forced_shame", true)
            putExtra("calorie_excess", calorieExcess)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messages = listOf(
            "Still over your limit. The shame doesn't go away.",
            "You think ignoring it makes it better? It doesn't.",
            "Every 5 minutes you stay over, you get this. Enjoy.",
            "Your future self is still disappointed.",
            "The shame persists. Deal with it.",
            "Still over budget. Time to face reality.",
            "Calories don't lie. Neither do I.",
            "You knew this was coming.",
            "The notification won't stop until you do.",
            "Reality check: you're still over your limit.",
            "Still stuffing your face?",
            "No self-control detected. Try again.",
            "That's still $calorieExcess extra calories sitting there.",
            "Your discipline is showing... and it's pathetic.",
            "Every 5 minutes. Like clockwork. Like your failure."
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("YOU WENT $calorieExcess CALORIES OVER")
            .setContentText(messages.random())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
