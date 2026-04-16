package com.lifeos.modules.lifeos_habittracker.data.repository

import com.lifeos.modules.lifeos_habittracker.data.local.HabitCheckIn
import com.lifeos.modules.lifeos_habittracker.data.local.HabitDao
import com.lifeos.modules.lifeos_habittracker.data.local.HabitEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitsRepository @Inject constructor(
    private val habitDao: HabitDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getAllHabits(): Flow<List<HabitEntity>> = habitDao.getAllHabits()

    suspend fun getHabitById(habitId: Long): HabitEntity? = habitDao.getHabitById(habitId)

    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habitId: Long) = habitDao.deleteHabitById(habitId)

    fun getCheckInsForHabit(habitId: Long): Flow<List<HabitCheckIn>> = 
        habitDao.getCheckInsForHabit(habitId)

    suspend fun isCheckedInToday(habitId: Long): Boolean {
        val today = LocalDate.now().format(dateFormatter)
        return habitDao.getCheckInForDate(habitId, today) != null
    }

    suspend fun toggleCheckIn(habitId: Long): Boolean {
        val today = LocalDate.now().format(dateFormatter)
        val existing = habitDao.getCheckInForDate(habitId, today)
        
        return if (existing != null) {
            habitDao.deleteCheckIn(existing)
            false
        } else {
            habitDao.insertCheckIn(HabitCheckIn(habitId = habitId, date = today))
            true
        }
    }

    suspend fun getCheckInCount(habitId: Long): Int = habitDao.getCheckInCount(habitId)

    suspend fun getStreak(habitId: Long): Int {
        val habit = habitDao.getHabitById(habitId) ?: return 0
        var streak = 0
        var currentDate = LocalDate.now()

        // If today applies but isn't checked in yet, start from yesterday
        // so the streak isn't reset until the day ends without a check-in
        if (habitAppliesToDate(habit, currentDate)) {
            val todayStr = currentDate.format(dateFormatter)
            if (habitDao.getCheckInForDate(habitId, todayStr) == null) {
                currentDate = currentDate.minusDays(1)
            }
        } else {
            currentDate = currentDate.minusDays(1)
        }

        while (true) {
            if (!habitAppliesToDate(habit, currentDate)) {
                currentDate = currentDate.minusDays(1)
                if (currentDate.isBefore(LocalDate.now().minusYears(1))) break
                continue
            }
            
            val dateStr = currentDate.format(dateFormatter)
            val checkIn = habitDao.getCheckInForDate(habitId, dateStr)
            
            if (checkIn != null) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }
        
        return streak
    }

    suspend fun getHabitsWithReminders(): List<HabitEntity> = habitDao.getHabitsWithReminders()

    private fun habitAppliesToDate(habit: HabitEntity, date: LocalDate): Boolean {
        return when (habit.frequency) {
            com.lifeos.modules.lifeos_habittracker.data.local.HabitFrequency.DAILY -> true
            com.lifeos.modules.lifeos_habittracker.data.local.HabitFrequency.SPECIFIC_DAYS -> {
                val dayOfWeek = date.dayOfWeek.value // 1 = Monday, 7 = Sunday
                habit.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(dayOfWeek)
            }
            com.lifeos.modules.lifeos_habittracker.data.local.HabitFrequency.TIMES_PER_WEEK -> true
        }
    }

    fun daysSinceStart(habit: HabitEntity): Int {
        val startDate = Instant.ofEpochMilli(habit.createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now()
        return (today.toEpochDay() - startDate.toEpochDay() + 1).toInt().coerceAtLeast(1)
    }
}
