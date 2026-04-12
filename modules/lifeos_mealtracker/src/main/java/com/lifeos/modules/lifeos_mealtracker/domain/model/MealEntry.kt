package com.lifeos.modules.lifeos_mealtracker.domain.model

import java.time.LocalDate

data class MealEntry(
    val id: Long = 0,
    val date: LocalDate,
    val mealType: MealType,
    val name: String? = null,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val createdAt: Long = System.currentTimeMillis()
)
