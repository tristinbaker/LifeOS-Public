package com.lifeos.modules.lifeos_habittracker.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitReminderScheduler @Inject constructor(
    private val context: Context
) {
    /**
     * Schedule (or reschedule) reminders for a habit.
     *
     * @param habitId  The habit's ID
     * @param habitName The habit's display name (shown in notification)
     * @param reminderTimeMillis Millis since midnight for the desired notification time
     * @param reminderDays Comma-separated Calendar day constants (e.g. "2,4,6" = Mon/Wed/Fri).
     *                     Empty string means every day.
     */
    fun scheduleReminders(
        habitId: Long,
        habitName: String,
        reminderTimeMillis: Long,
        reminderDays: String
    ) {
        cancelReminders(habitId)

        // App uses 1=Mon..7=Sun; Calendar uses SUNDAY=1, MONDAY=2..SATURDAY=7
        // Convert: calendarDay = (appDay % 7) + 1
        val allDays = listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                             Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
        val days = if (reminderDays.isBlank()) {
            allDays
        } else {
            reminderDays.split(",").mapNotNull { it.trim().toIntOrNull()?.let { d -> (d % 7) + 1 } }
        }

        days.forEach { dayOfWeek ->
            val delay = calculateDelayUntil(reminderTimeMillis, dayOfWeek)
            val data = Data.Builder()
                .putString(HabitReminderWorker.KEY_HABIT_NAME, habitName)
                .putLong(HabitReminderWorker.KEY_HABIT_ID, habitId)
                .build()

            val request = PeriodicWorkRequestBuilder<HabitReminderWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(workTag(habitId))
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "${workTag(habitId)}_day_$dayOfWeek",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }

    fun cancelReminders(habitId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag(workTag(habitId))
    }

    private fun workTag(habitId: Long) = "habit_reminder_$habitId"

    private fun calculateDelayUntil(reminderTimeMillis: Long, targetDayOfWeek: Int): Long {
        // reminderTimeMillis is a full epoch timestamp — extract hour/minute from it
        val timeCal = Calendar.getInstance().apply { timeInMillis = reminderTimeMillis }
        val hour = timeCal.get(Calendar.HOUR_OF_DAY)
        val minute = timeCal.get(Calendar.MINUTE)

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, targetDayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the target time is in the past, advance by 7 days
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 7)
        }

        return target.timeInMillis - now.timeInMillis
    }
}
