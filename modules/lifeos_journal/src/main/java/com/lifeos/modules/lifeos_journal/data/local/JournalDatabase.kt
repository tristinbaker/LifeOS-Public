package com.lifeos.modules.lifeos_journal.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val JOURNAL_MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `journal_images` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`entryId` INTEGER NOT NULL, " +
                "`localPath` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)"
        )
    }
}

@Database(
    entities = [JournalEntryEntity::class, JournalSettingsEntity::class, JournalImageEntity::class],
    version = 2,
    exportSchema = true
)
abstract class JournalDatabase : RoomDatabase() {
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun journalSettingsDao(): JournalSettingsDao
    abstract fun journalImageDao(): JournalImageDao
}
