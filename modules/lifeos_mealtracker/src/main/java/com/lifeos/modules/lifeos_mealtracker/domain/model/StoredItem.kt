package com.lifeos.modules.lifeos_mealtracker.domain.model

data class StoredItem(
    val id: Long = 0,
    val name: String,
    val caloriesPerUnit: Int,
    val proteinPerUnit: Int,
    val carbsPerUnit: Int,
    val fatPerUnit: Int
)
