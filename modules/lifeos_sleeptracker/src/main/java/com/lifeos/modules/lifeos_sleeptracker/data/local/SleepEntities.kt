package com.lifeos.modules.lifeos_sleeptracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lifeos_sleeptracker_sleep_logs")
data class SleepLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // yyyy-MM-dd format
    val startTime: Long, // epoch millis
    val endTime: Long, // epoch millis
    val quality: Float, // 0.5 to 5.0, increments of 0.5
    val dreamNotes: String = "",
    val notes: String = "",
    val sleepMedicationTaken: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lifeos_sleeptracker_settings")
data class SleepSettingsEntity(
    @PrimaryKey
    val id: Int = 1, // Singleton - always 1
    val reminderEnabled: Boolean = false,
    val reminderTime: Long = 22 * 60 * 60 * 1000 // Default 10:00 PM in millis since midnight
)