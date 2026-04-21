package com.lifeos.modules.lifeos_sleeptracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SleepLogEntity::class, SleepSettingsEntity::class],
    version = 2,
    exportSchema = true
)
abstract class SleepDatabase : RoomDatabase() {
    abstract fun sleepLogDao(): SleepLogDao
    abstract fun sleepSettingsDao(): SleepSettingsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE lifeos_sleeptracker_sleep_logs ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE lifeos_sleeptracker_sleep_logs ADD COLUMN sleepMedicationTaken INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}