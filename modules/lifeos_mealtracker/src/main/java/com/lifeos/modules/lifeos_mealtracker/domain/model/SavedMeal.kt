package com.lifeos.modules.lifeos_mealtracker.domain.model

data class SavedMeal(
    val id: Long = 0,
    val name: String,
    val mealType: MealType,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)
