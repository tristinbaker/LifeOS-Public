package com.lifeos.modules.lifeos_aiinsights.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CachedReportEntity::class], version = 1)
abstract class AiInsightsDatabase : RoomDatabase() {
    abstract fun cachedReportDao(): CachedReportDao
}
