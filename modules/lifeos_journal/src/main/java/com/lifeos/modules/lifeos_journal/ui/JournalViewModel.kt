package com.lifeos.modules.lifeos_journal.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_journal.data.local.JournalEntryEntity
import com.lifeos.modules.lifeos_journal.data.local.JournalImageEntity
import com.lifeos.modules.lifeos_journal.data.local.JournalSettingsEntity
import com.lifeos.modules.lifeos_journal.data.repository.JournalRepository
import com.lifeos.modules.lifeos_journal.data.repository.WeeklyStats
import com.lifeos.modules.lifeos_journal.notification.JournalReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class JournalUiState(
    val entries: List<JournalEntryEntity> = emptyList(),
    val weeklyStats: WeeklyStats = WeeklyStats(0),
    val settings: JournalSettingsEntity? = null,
    val currentEntryImages: List<JournalImageEntity> = emptyList(),
    val weeklyImages: List<JournalImageEntity> = emptyList()
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository,
    private val reminderScheduler: JournalReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    private var imagesJob: Job? = null

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

    fun loadImagesForEntry(entryId: Long?) {
        imagesJob?.cancel()
        if (entryId == null || entryId <= 0) {
            _uiState.update { it.copy(currentEntryImages = emptyList()) }
            return
        }
        imagesJob = viewModelScope.launch {
            repository.getImagesForEntry(entryId).collect { images ->
                _uiState.update { it.copy(currentEntryImages = images) }
            }
        }
    }

    fun saveEntry(
        id: Long?,
        date: String,
        content: String,
        mood: Int,
        newImageUris: List<Uri> = emptyList(),
        removedImageIds: Set<Long> = emptySet(),
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val entry = JournalEntryEntity(
                id = id ?: 0,
                date = date,
                content = content,
                mood = mood
            )

            val entryId = if (id != null && id > 0) {
                repository.updateEntry(entry)
                id
            } else {
                repository.insertEntry(entry)
            }

            removedImageIds.forEach { imageId ->
                repository.deleteImage(imageId)
            }

            if (newImageUris.isNotEmpty()) {
                repository.saveImages(entryId, newImageUris)
            }

            onComplete()
        }
    }

    fun deleteEntry(entryId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAllImagesForEntry(entryId)
            repository.deleteEntry(entryId)
            onComplete()
        }
    }

    fun loadWeeklyImages() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val daysFromMonday = (today.dayOfWeek.value - 1).toLong()
            val weekStart = today.minusDays(daysFromMonday)
            val weekEnd = weekStart.plusDays(6)
            val images = repository.getWeeklyImages(weekStart, weekEnd)
            _uiState.update { it.copy(weeklyImages = images) }
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
