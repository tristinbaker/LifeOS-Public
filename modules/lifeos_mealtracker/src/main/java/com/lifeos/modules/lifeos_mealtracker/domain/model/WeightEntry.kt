package com.lifeos.modules.lifeos_mealtracker.domain.model

import java.time.LocalDate

data class WeightEntry(
    val id: Long = 0,
    val date: LocalDate,
    val weight: Double,
    val createdAt: Long = System.currentTimeMillis()
)
