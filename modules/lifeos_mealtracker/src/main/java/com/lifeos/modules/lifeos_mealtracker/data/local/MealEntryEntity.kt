package com.lifeos.modules.lifeos_mealtracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealEntry
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealType
import java.time.LocalDate

@Entity(tableName = "lifeos_mealtracker_meal_entries")
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val mealType: MealType,
    val name: String?,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val createdAt: Long
) {
    fun toDomain() = MealEntry(id, date, mealType, name, calories, protein, carbs, fat, createdAt)
}

fun MealEntry.toEntity() = MealEntryEntity(id, date, mealType, name, calories, protein, carbs, fat, createdAt)
