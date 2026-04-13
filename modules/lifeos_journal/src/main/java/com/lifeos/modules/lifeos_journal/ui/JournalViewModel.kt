package com.lifeos.modules.lifeos_journal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_journal.data.local.JournalEntryEntity
import com.lifeos.modules.lifeos_journal.data.local.JournalSettingsEntity
import com.lifeos.modules.lifeos_journal.data.repository.JournalRepository
import com.lifeos.modules.lifeos_journal.data.repository.WeeklyStats
import com.lifeos.modules.lifeos_journal.notification.JournalReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JournalUiState(
    val entries: List<JournalEntryEntity> = emptyList(),
    val weeklyStats: WeeklyStats = WeeklyStats(0),
    val settings: JournalSettingsEntity? = null
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository,
    private val reminderScheduler: JournalReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
                val weeklyStats = repository.getWeeklyStats()
                _uiState.update { it.copy(entries = entries, weeklyStats = weeklyStats) }
            }
        }
        viewModelScope.launch {
            repository.getSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun saveEntry(
        id: Long?,
        date: String,
        content: String,
        mood: Int,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val entry = JournalEntryEntity(
                id = id ?: 0,
                date = date,
                content = content,
                mood = mood
            )

            if (id != null && id > 0) {
                repository.updateEntry(entry)
            } else {
                repository.insertEntry(entry)
            }

            onComplete()
        }
    }

    fun deleteEntry(entryId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
            onComplete()
        }
    }

    suspend fun getEntryById(entryId: Long): JournalEntryEntity? {
        return repository.getEntryById(entryId)
    }

    fun updateSettings(enabled: Boolean, reminderTime: Long) {
        viewModelScope.launch {
            val settings = JournalSettingsEntity(
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