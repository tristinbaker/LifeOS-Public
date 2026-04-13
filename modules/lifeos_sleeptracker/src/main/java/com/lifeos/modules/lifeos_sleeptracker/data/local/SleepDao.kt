package com.lifeos.modules.lifeos_sleeptracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepLogDao {
    @Query("SELECT * FROM lifeos_sleeptracker_sleep_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<SleepLogEntity>>

    @Query("SELECT * FROM lifeos_sleeptracker_sleep_logs WHERE id = :logId")
    suspend fun getLogById(logId: Long): SleepLogEntity?

    @Query("SELECT * FROM lifeos_sleeptracker_sleep_logs WHERE date = :date LIMIT 1")
    suspend fun getLogForDate(date: String): SleepLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SleepLogEntity): Long

    @Update
    suspend fun updateLog(log: SleepLogEntity)

    @Delete
    suspend fun deleteLog(log: SleepLogEntity)

    @Query("DELETE FROM lifeos_sleeptracker_sleep_logs WHERE id = :logId")
    suspend fun deleteLogById(logId: Long)

    @Query("SELECT * FROM lifeos_sleeptracker_sleep_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getLogsBetween(startDate: String, endDate: String): List<SleepLogEntity>
}

@Dao
interface SleepSettingsDao {
    @Query("SELECT * FROM lifeos_sleeptracker_settings WHERE id = 1")
    fun getSettings(): Flow<SleepSettingsEntity?>

    @Query("SELECT * FROM lifeos_sleeptracker_settings WHERE id = 1")
    suspend fun getSettingsOnce(): SleepSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SleepSettingsEntity)

    @Update
    suspend fun updateSettings(settings: SleepSettingsEntity)
}