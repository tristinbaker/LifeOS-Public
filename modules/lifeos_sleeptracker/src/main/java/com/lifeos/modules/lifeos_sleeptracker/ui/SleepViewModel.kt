package com.lifeos.modules.lifeos_sleeptracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_sleeptracker.data.local.SleepLogEntity
import com.lifeos.modules.lifeos_sleeptracker.data.local.SleepSettingsEntity
import com.lifeos.modules.lifeos_sleeptracker.data.repository.SleepRepository
import com.lifeos.modules.lifeos_sleeptracker.data.repository.WeeklyStats
import com.lifeos.modules.lifeos_sleeptracker.notification.SleepReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SleepLogWithDuration(
    val log: SleepLogEntity,
    val durationHours: Float
)

data class SleepUiState(
    val logs: List<SleepLogWithDuration> = emptyList(),
    val weeklyStats: WeeklyStats = WeeklyStats(0f, 0f, 0),
    val settings: SleepSettingsEntity? = null
)

@HiltViewModel
class SleepViewModel @Inject constructor(
    private val repository: SleepRepository,
    private val reminderScheduler: SleepReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllLogs().collect { logs ->
                val logsWithDuration = logs.map { log ->
                    SleepLogWithDuration(
                        log = log,
                        durationHours = repository.calculateDurationHours(log.startTime, log.endTime)
                    )
                }
                val weeklyStats = repository.getWeeklyStats()
                _uiState.update { it.copy(logs = logsWithDuration, weeklyStats = weeklyStats) }
            }
        }
        viewModelScope.launch {
            repository.getSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun saveLog(
        id: Long?,
        date: String,
        startTime: Long,
        endTime: Long,
        quality: Float,
        dreamNotes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val log = SleepLogEntity(
                id = id ?: 0,
                date = date,
                startTime = startTime,
                endTime = endTime,
                quality = quality,
                dreamNotes = dreamNotes
            )

            if (id != null && id > 0) {
                repository.updateLog(log)
            } else {
                repository.insertLog(log)
            }

            onComplete()
        }
    }

    fun deleteLog(logId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteLog(logId)
            onComplete()
        }
    }

    suspend fun getLogById(logId: Long): SleepLogEntity? {
        return repository.getLogById(logId)
    }

    fun updateSettings(enabled: Boolean, reminderTime: Long) {
        viewModelScope.launch {
            val settings = SleepSettingsEntity(
                id = 1,
                reminderEnabled = enabled,
                reminderTime = reminderTime
            )
            repository.updateSettings(settings)

            if (enabled) {
                reminderScheduler.scheduleReminder(reminderTime)
            } else {
                reminderScheduler.cancelReminder()
            }
        }
    }
}