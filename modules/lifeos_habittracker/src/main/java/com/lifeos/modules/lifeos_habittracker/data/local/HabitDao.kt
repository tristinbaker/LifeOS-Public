package com.lifeos.modules.lifeos_habittracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM lifeos_habittracker_habits WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM lifeos_habittracker_habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM lifeos_habittracker_habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Long)

    @Query("SELECT * FROM lifeos_habittracker_checkins WHERE habitId = :habitId ORDER BY date DESC")
    fun getCheckInsForHabit(habitId: Long): Flow<List<HabitCheckIn>>

    @Query("SELECT * FROM lifeos_habittracker_checkins WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCheckInForDate(habitId: Long, date: String): HabitCheckIn?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: HabitCheckIn)

    @Delete
    suspend fun deleteCheckIn(checkIn: HabitCheckIn)

    @Query("DELETE FROM lifeos_habittracker_checkins WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCheckInForDate(habitId: Long, date: String)

    @Query("SELECT COUNT(*) FROM lifeos_habittracker_checkins WHERE habitId = :habitId")
    suspend fun getCheckInCount(habitId: Long): Int

    @Query("SELECT * FROM lifeos_habittracker_checkins WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getCheckInsBetween(habitId: Long, startDate: String, endDate: String): List<HabitCheckIn>

    @Query("SELECT * FROM lifeos_habittracker_habits WHERE reminderEnabled = 1 AND reminderTime IS NOT NULL")
    suspend fun getHabitsWithReminders(): List<HabitEntity>
}
