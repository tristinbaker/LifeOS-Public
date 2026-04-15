package com.lifeos.modules.lifeos_journal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lifeos_journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // yyyy-MM-dd format
    val content: String,
    val mood: Int, // 1-5
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lifeos_journal_settings")
data class JournalSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val reminderEnabled: Boolean = false,
    val reminderTime: Long = 21 * 60 * 60 * 1000 // Default 9:00 PM
)

@Entity(tableName = "journal_images")
data class JournalImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryId: Long,
    val localPath: String,
    val createdAt: Long = System.currentTimeMillis()
)