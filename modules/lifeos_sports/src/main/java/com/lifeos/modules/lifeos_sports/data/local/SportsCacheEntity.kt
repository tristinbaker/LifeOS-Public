package com.lifeos.modules.lifeos_sports.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sports_cache")
data class SportsCacheEntity(
    @PrimaryKey val key: String,
    val data: String,
    val cachedDate: String
)
