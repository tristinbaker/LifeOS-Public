package com.lifeos.modules.lifeos_journal.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM lifeos_journal_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM lifeos_journal_entries WHERE id = :entryId")
    suspend fun getEntryById(entryId: Long): JournalEntryEntity?

    @Query("SELECT * FROM lifeos_journal_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryForDate(date: String): JournalEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: JournalEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: JournalEntryEntity)

    @Query("DELETE FROM lifeos_journal_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: Long)

    @Query("SELECT * FROM lifeos_journal_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getEntriesBetween(startDate: String, endDate: String): List<JournalEntryEntity>

    @Query("SELECT * FROM lifeos_journal_entries WHERE content LIKE '%' || :query || '%' ORDER BY date DESC")
    suspend fun searchEntries(query: String): List<JournalEntryEntity>
}

@Dao
interface JournalSettingsDao {
    @Query("SELECT * FROM lifeos_journal_settings WHERE id = 1")
    fun getSettings(): Flow<JournalSettingsEntity?>

    @Query("SELECT * FROM lifeos_journal_settings WHERE id = 1")
    suspend fun getSettingsOnce(): JournalSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: JournalSettingsEntity)

    @Update
    suspend fun updateSettings(settings: JournalSettingsEntity)
}

@Dao
interface JournalImageDao {
    @Query("SELECT * FROM journal_images WHERE entryId = :entryId ORDER BY createdAt ASC")
    fun getImagesForEntry(entryId: Long): Flow<List<JournalImageEntity>>

    @Query("SELECT * FROM journal_images WHERE entryId = :entryId ORDER BY createdAt ASC")
    suspend fun getImagesForEntryOnce(entryId: Long): List<JournalImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: JournalImageEntity): Long

    @Query("SELECT localPath FROM journal_images WHERE id = :imageId")
    suspend fun getLocalPath(imageId: Long): String?

    @Query("DELETE FROM journal_images WHERE id = :imageId")
    suspend fun deleteImage(imageId: Long)

    @Query("DELETE FROM journal_images WHERE entryId = :entryId")
    suspend fun deleteAllImagesForEntry(entryId: Long)
}