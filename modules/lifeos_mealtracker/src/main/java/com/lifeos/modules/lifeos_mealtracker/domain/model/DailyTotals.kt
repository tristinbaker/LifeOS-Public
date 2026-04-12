package com.lifeos.modules.lifeos_mealtracker.domain.model

import java.time.LocalDate

data class DailyTotals(
    val date: LocalDate,
    val totalCalories: Int,
    val totalProtein: Int,
    val totalCarbs: Int,
    val totalFat: Int,
    val meals: List<MealEntry>
)
