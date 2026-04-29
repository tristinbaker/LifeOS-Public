package com.lifeos.modules.lifeos_journal.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_journal.data.local.JournalEntryEntity
import com.lifeos.modules.lifeos_journal.data.local.JournalImageEntity
import com.lifeos.modules.lifeos_journal.data.local.JournalSettingsEntity
import com.lifeos.modules.lifeos_journal.data.repository.JournalRepository
import com.lifeos.modules.lifeos_journal.data.repository.JournalStats
import com.lifeos.modules.lifeos_journal.notification.JournalReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class JournalUiState(
    val entries: List<JournalEntryEntity> = emptyList(),
    val journalStats: JournalStats = JournalStats(),
    val settings: JournalSettingsEntity? = null,
    val currentEntryImages: List<JournalImageEntity> = emptyList(),
    val weeklyImages: List<JournalImageEntity> = emptyList(),
    val weekOffset: Int = 0,
    val weekLabel: String = "",
    val weekReviewLoading: Boolean = false
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
        viewModelScope.launch { repository.compressExistingImages() }
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
                _uiState.update { it.copy(entries = entries, journalStats = computeStats(entries)) }
            }
        }
        viewModelScope.launch {
            repository.getSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    private fun computeStats(entries: List<JournalEntryEntity>): JournalStats {
        if (entries.isEmpty()) return JournalStats()
        val entryDates = entries.mapNotNull {
            try { LocalDate.parse(it.date) } catch (_: Exception) { null }
        }.toSet()
        var streak = 0
        var day = LocalDate.now()
        if (!entryDates.contains(day)) day = day.minusDays(1)
        while (entryDates.contains(day)) { streak++; day = day.minusDays(1) }
        val avgWords = entries.map { it.content.trim().split("\\s+".toRegex()).count { w -> w.isNotEmpty() } }.average().toInt()
        val avgMood = entries.map { it.mood }.average().toFloat()
        return JournalStats(streak = streak, avgWordCount = avgWords, totalEntries = entries.size, avgMood = avgMood)
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

    fun loadWeeklyImages() = loadWeeklyImagesForOffset(0)

    fun navigateWeek(delta: Int) {
        val newOffset = (_uiState.value.weekOffset + delta).coerceAtMost(0)
        loadWeeklyImagesForOffset(newOffset)
    }

    private fun loadWeeklyImagesForOffset(offsetWeeks: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(weekReviewLoading = true, weekOffset = offsetWeeks) }
            val today = LocalDate.now()
            val currentMonday = today.minusDays((today.dayOfWeek.value - 1).toLong())
            val weekStart = currentMonday.plusWeeks(offsetWeeks.toLong())
            val weekEnd = weekStart.plusDays(6)
            val fmt = DateTimeFormatter.ofPattern("MMM d")
            val label = "${weekStart.format(fmt)} – ${weekEnd.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
            val images = repository.getWeeklyImages(weekStart, weekEnd)
            _uiState.update { it.copy(weeklyImages = images, weekLabel = label, weekReviewLoading = false) }
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
