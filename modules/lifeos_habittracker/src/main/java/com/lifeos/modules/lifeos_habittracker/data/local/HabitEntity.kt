package com.lifeos.modules.lifeos_habittracker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class HabitFrequency {
    DAILY,
    SPECIFIC_DAYS,
    TIMES_PER_WEEK
}

@Entity(tableName = "lifeos_habittracker_habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val daysOfWeek: String = "", // Comma-separated: "1,3,5" = Mon,Wed,Fri
    val timesPerWeek: Int = 7,
    val reminderTime: Long? = null, // Millis since midnight
    val reminderEnabled: Boolean = false,
    val reminderDays: String = "", // Comma-separated Calendar day constants: "2,3,4,5,6" = Mon-Fri; empty = every day
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

@Entity(
    tableName = "lifeos_habittracker_checkins",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitCheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // yyyy-MM-dd format
    val createdAt: Long = System.currentTimeMillis()
)
