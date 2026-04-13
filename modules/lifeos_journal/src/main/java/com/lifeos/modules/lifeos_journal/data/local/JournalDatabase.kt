package com.lifeos.modules.lifeos_journal.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [JournalEntryEntity::class, JournalSettingsEntity::class],
    version = 1,
    exportSchema = true
)
abstract class JournalDatabase : RoomDatabase() {
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun journalSettingsDao(): JournalSettingsDao
}