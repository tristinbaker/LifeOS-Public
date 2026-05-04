package com.lifeos.modules.lifeos_aiinsights.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lifeos_aiinsights_cached_reports",
    indices = [Index(value = ["reportType", "periodStart"], unique = true)]
)
data class CachedReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportType: String,
    val periodStart: String,
    val generatedAt: Long,
    val content: String
)
