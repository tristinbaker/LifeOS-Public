package com.lifeos.modules.lifeos_aiinsights.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedReportDao {
    @Query("SELECT * FROM lifeos_aiinsights_cached_reports WHERE reportType = :type AND periodStart = :start LIMIT 1")
    suspend fun getReport(type: String, start: String): CachedReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReport(report: CachedReportEntity)
}
