package com.lifeos.modules.lifeos_sleeptracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SleepLogEntity::class, SleepSettingsEntity::class],
    version = 1,
    exportSchema = true
)
abstract class SleepDatabase : RoomDatabase() {
    abstract fun sleepLogDao(): SleepLogDao
    abstract fun sleepSettingsDao(): SleepSettingsDao
}