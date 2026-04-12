package com.lifeos.modules.lifeos_mealtracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.modules.lifeos_mealtracker.domain.model.WeightEntry
import java.time.LocalDate

@Entity(tableName = "lifeos_mealtracker_weight_entries")
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val weight: Double,
    val createdAt: Long
) {
    fun toDomain() = WeightEntry(id, date, weight, createdAt)
}

fun WeightEntry.toEntity() = WeightEntryEntity(id, date, weight, createdAt)
