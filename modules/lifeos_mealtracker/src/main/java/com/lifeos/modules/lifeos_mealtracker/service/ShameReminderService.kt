package com.lifeos.modules.lifeos_mealtracker.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lifeos.modules.lifeos_mealtracker.data.local.AppDatabase
import com.lifeos.modules.lifeos_mealtracker.data.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class ShameReminderService : Service() {

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        if (isRunning) return
        isRunning = true

        NotificationHelper.cancelNotification(this)

        serviceScope.launch {
            while (isRunning) {
                checkCaloriesAndNotify()
                delay(5 * 60 * 1000L)
            }
        }
    }

    private suspend fun checkCaloriesAndNotify() {
        try {
            val settings = settingsRepository.settings.first()
            val today = LocalDate.now()

            val meals = database.mealEntryDao().getMealsByDate(today).first()
            val totalCalories = meals.sumOf { it.calories }

            if (totalCalories > settings.dailyCalorieGoal) {
                val excess = totalCalories - settings.dailyCalorieGoal
                NotificationHelper.showShameNotification(this@ShameReminderService, excess)
            } else {
                NotificationHelper.cancelNotification(this@ShameReminderService)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "com.lifeos.modules.lifeos_mealtracker.START_SHAME_SERVICE"
        const val ACTION_STOP = "com.lifeos.modules.lifeos_mealtracker.STOP_SHAME_SERVICE"
    }
}
