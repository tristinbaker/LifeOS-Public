package com.lifeos.modules.lifeos_mealtracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.modules.lifeos_mealtracker.domain.model.SavedMeal
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealType

@Entity(tableName = "lifeos_mealtracker_saved_meals")
data class SavedMealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mealType: MealType,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
) {
    fun toDomain() = SavedMeal(id, name, mealType, calories, protein, carbs, fat)
}

fun SavedMeal.toEntity() = SavedMealEntity(id, name, mealType, calories, protein, carbs, fat)
