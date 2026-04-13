package com.lifeos.modules.lifeos_journal.data.repository

import com.lifeos.modules.lifeos_journal.data.local.JournalEntryDao
import com.lifeos.modules.lifeos_journal.data.local.JournalEntryEntity
import com.lifeos.modules.lifeos_journal.data.local.JournalSettingsDao
import com.lifeos.modules.lifeos_journal.data.local.JournalSettingsEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(
    private val journalEntryDao: JournalEntryDao,
    private val journalSettingsDao: JournalSettingsDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getAllEntries(): Flow<List<JournalEntryEntity>> = journalEntryDao.getAllEntries()

    suspend fun getEntryById(entryId: Long): JournalEntryEntity? = journalEntryDao.getEntryById(entryId)

    suspend fun getEntryForDate(date: LocalDate): JournalEntryEntity? = 
        journalEntryDao.getEntryForDate(date.format(dateFormatter))

    suspend fun insertEntry(entry: JournalEntryEntity): Long = journalEntryDao.insertEntry(entry)

    suspend fun updateEntry(entry: JournalEntryEntity) = journalEntryDao.updateEntry(entry)

    suspend fun deleteEntry(entryId: Long) = journalEntryDao.deleteEntryById(entryId)

    suspend fun searchEntries(query: String): List<JournalEntryEntity> = journalEntryDao.searchEntries(query)

    suspend fun getWeeklyStats(): WeeklyStats {
        val today = LocalDate.now()
        val weekStart = today.minusDays(6)
        val entries = journalEntryDao.getEntriesBetween(
            weekStart.format(dateFormatter),
            today.format(dateFormatter)
        )
        return WeeklyStats(entries.size)
    }

    fun getSettings(): Flow<JournalSettingsEntity?> = journalSettingsDao.getSettings()

    suspend fun getSettingsOnce(): JournalSettingsEntity? = journalSettingsDao.getSettingsOnce()

    suspend fun updateSettings(settings: JournalSettingsEntity) {
        journalSettingsDao.insertSettings(settings)
    }
}

data class WeeklyStats(
    val daysLogged: Int
)