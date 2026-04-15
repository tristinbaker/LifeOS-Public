package com.lifeos.modules.lifeos_sleeptracker.data.repository

import com.lifeos.modules.lifeos_sleeptracker.data.local.SleepLogDao
import com.lifeos.modules.lifeos_sleeptracker.data.local.SleepLogEntity
import com.lifeos.modules.lifeos_sleeptracker.data.local.SleepSettingsDao
import com.lifeos.modules.lifeos_sleeptracker.data.local.SleepSettingsEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepository @Inject constructor(
    private val sleepLogDao: SleepLogDao,
    private val sleepSettingsDao: SleepSettingsDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getAllLogs(): Flow<List<SleepLogEntity>> = sleepLogDao.getAllLogs()

    suspend fun getLogById(logId: Long): SleepLogEntity? = sleepLogDao.getLogById(logId)

    suspend fun getLogForDate(date: LocalDate): SleepLogEntity? = 
        sleepLogDao.getLogForDate(date.format(dateFormatter))

    suspend fun insertLog(log: SleepLogEntity): Long = sleepLogDao.insertLog(log)

    suspend fun updateLog(log: SleepLogEntity) = sleepLogDao.updateLog(log)

    suspend fun deleteLog(logId: Long) = sleepLogDao.deleteLogById(logId)

    suspend fun getLogsBetween(startDate: LocalDate, endDate: LocalDate): List<SleepLogEntity> =
        sleepLogDao.getLogsBetween(
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        )

    fun calculateDurationHours(startTime: Long, endTime: Long): Float {
        var durationMs = endTime - startTime
        if (durationMs < 0) {
            durationMs += 24 * 60 * 60 * 1000 // Bedtime was yesterday
        }
        return (durationMs.toFloat() / (1000 * 60 * 60))
    }

    suspend fun getWeeklyStats(): WeeklyStats {
        val today = LocalDate.now()
        val weekStart = today.minusDays(6)
        val logs = getLogsBetween(weekStart, today)

        if (logs.isEmpty()) return WeeklyStats(0f, 0f, 0)

        val totalHours = logs.sumOf { calculateDurationHours(it.startTime, it.endTime).toDouble() }.toFloat()
        val avgQuality = logs.map { it.quality }.average().toFloat()

        val avgBedTime = calculateAverageTime(logs.map { it.startTime })
        val avgWakeTime = calculateAverageTime(logs.map { it.endTime })

        return WeeklyStats(avgQuality, totalHours, logs.size, avgBedTime, avgWakeTime)
    }

    private fun calculateAverageTime(times: List<Long>): String {
        if (times.isEmpty()) return "--:--"

        val hours = times.map { time ->
            val localTime = LocalTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault())
            localTime.hour + localTime.minute / 60.0
        }
        val avgHours = circularMean(hours)
        val avgTime = LocalTime.of((avgHours.toInt() + 24) % 24, ((avgHours % 1.0) * 60).toInt())

        return avgTime.format(DateTimeFormatter.ofPattern("h:mm a"))
    }

    private fun circularMean(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0

        val sumSin = values.sumOf { kotlin.math.sin(2.0 * Math.PI * it / 24.0) }
        val sumCos = values.sumOf { kotlin.math.cos(2.0 * Math.PI * it / 24.0) }
        val meanAngle = kotlin.math.atan2(sumSin, sumCos)
        var result = meanAngle * 24.0 / (2.0 * Math.PI)

        if (result < 0) result += 24.0
        return result
    }

    fun getSettings(): Flow<SleepSettingsEntity?> = sleepSettingsDao.getSettings()

    suspend fun getSettingsOnce(): SleepSettingsEntity? = sleepSettingsDao.getSettingsOnce()

    suspend fun updateSettings(settings: SleepSettingsEntity) {
        sleepSettingsDao.insertSettings(settings)
    }
}

data class WeeklyStats(
    val avgQuality: Float,
    val totalHours: Float,
    val daysLogged: Int,
    val avgBedTime: String = "--:--",
    val avgWakeTime: String = "--:--"
)