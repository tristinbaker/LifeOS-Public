package com.lifeos.modules.lifeos_mealtracker.domain.usecase

import com.lifeos.modules.lifeos_mealtracker.data.repository.WeightRepository
import com.lifeos.modules.lifeos_mealtracker.domain.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class GoalEstimation(
    val estimatedDate: LocalDate?,
    val weeksRemaining: Double?,
    val isGaining: Boolean
)

class EstimateGoalDateUseCase @Inject constructor(
    private val weightRepository: WeightRepository
) {
    operator fun invoke(goalWeight: Double?, weeklyRate: Double): Flow<GoalEstimation?> {
        return weightRepository.getAllWeightEntries().map { entries ->
            if (goalWeight == null || entries.size < 2) {
                return@map null
            }

            val sortedEntries = entries.sortedBy { it.date }
            val firstEntry = sortedEntries.first()
            val lastEntry = sortedEntries.last()

            val currentWeight = lastEntry.weight
            val startWeight = firstEntry.weight
            val daysBetween = ChronoUnit.DAYS.between(firstEntry.date, lastEntry.date)

            if (daysBetween == 0L) return@map null

            val totalWeightChange = currentWeight - startWeight
            val weeklyChange = (totalWeightChange / daysBetween) * 7

            if (weeklyChange == 0.0) return@map null

            val isGaining = totalWeightChange > 0
            val targetChange = if (isGaining) goalWeight - currentWeight else currentWeight - goalWeight

            if (targetChange <= 0) {
                return@map GoalEstimation(
                    estimatedDate = LocalDate.now(),
                    weeksRemaining = 0.0,
                    isGaining = isGaining
                )
            }

            val weeksRemaining = targetChange / kotlin.math.abs(weeklyChange)
            val estimatedDate = LocalDate.now().plusDays((weeksRemaining * 7).toLong())

            GoalEstimation(
                estimatedDate = estimatedDate,
                weeksRemaining = weeksRemaining,
                isGaining = isGaining
            )
        }
    }
}
