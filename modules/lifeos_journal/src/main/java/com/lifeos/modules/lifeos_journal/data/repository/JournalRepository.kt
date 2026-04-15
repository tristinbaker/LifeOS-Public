package com.lifeos.modules.lifeos_journal.data.repository

import android.content.Context
import android.net.Uri
import com.lifeos.modules.lifeos_journal.data.local.JournalEntryDao
import com.lifeos.modules.lifeos_journal.data.local.JournalEntryEntity
import com.lifeos.modules.lifeos_journal.data.local.JournalImageDao
import com.lifeos.modules.lifeos_journal.data.local.JournalImageEntity
import com.lifeos.modules.lifeos_journal.data.local.JournalSettingsDao
import com.lifeos.modules.lifeos_journal.data.local.JournalSettingsEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val journalEntryDao: JournalEntryDao,
    private val journalSettingsDao: JournalSettingsDao,
    private val journalImageDao: JournalImageDao
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

    // Images

    fun getImagesForEntry(entryId: Long): Flow<List<JournalImageEntity>> =
        journalImageDao.getImagesForEntry(entryId)

    suspend fun saveImages(entryId: Long, uris: List<Uri>): List<JournalImageEntity> {
        val dir = File(context.filesDir, "journal_images").also { it.mkdirs() }
        return uris.mapIndexed { index, uri ->
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val ext = when (mimeType) {
                "image/png" -> "png"
                "image/gif" -> "gif"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val file = File(dir, "${System.currentTimeMillis()}_$index.$ext")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            val entity = JournalImageEntity(
                entryId = entryId,
                localPath = file.absolutePath,
                createdAt = System.currentTimeMillis()
            )
            val id = journalImageDao.insertImage(entity)
            entity.copy(id = id)
        }
    }

    suspend fun deleteImage(imageId: Long) {
        val path = journalImageDao.getLocalPath(imageId)
        path?.let { File(it).delete() }
        journalImageDao.deleteImage(imageId)
    }

    suspend fun deleteAllImagesForEntry(entryId: Long) {
        val images = journalImageDao.getImagesForEntryOnce(entryId)
        images.forEach { File(it.localPath).delete() }
        journalImageDao.deleteAllImagesForEntry(entryId)
    }
}

data class WeeklyStats(
    val daysLogged: Int
)
