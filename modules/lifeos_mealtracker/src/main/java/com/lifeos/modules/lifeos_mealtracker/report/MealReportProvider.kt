package com.lifeos.modules.lifeos_mealtracker.report

import com.lifeos.core.ModuleReport
import com.lifeos.core.ReportDataProvider
import com.lifeos.modules.lifeos_mealtracker.data.repository.MealRepository
import com.lifeos.modules.lifeos_mealtracker.data.repository.WeightRepository
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealEntry
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MealReportProvider @Inject constructor(
    private val mealRepository: MealRepository,
    private val weightRepository: WeightRepository
) : ReportDataProvider {

    override val sectionName = "nutrition"

    override suspend fun getWeeklyReport(startDate: LocalDate, endDate: LocalDate): ModuleReport {
        val days = startDate.until(endDate).days + 1
        return ModuleReport(
            sectionTitle = "Nutrition (${startDate.format(DateTimeFormatter.ofPattern("MMM d"))} – ${endDate.format(DateTimeFormatter.ofPattern("MMM d"))})",
            textBlock = buildSummary(startDate, endDate, days)
        )
    }

    override suspend fun getMonthlyReport(yearMonth: YearMonth): ModuleReport {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        return ModuleReport(
            sectionTitle = "Nutrition (${yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))})",
            textBlock = buildSummary(start, end, yearMonth.lengthOfMonth())
        )
    }

    private suspend fun buildSummary(startDate: LocalDate, endDate: LocalDate, totalDays: Int): String {
        val meals = mealRepository.getMealsBetweenDates(startDate, endDate).first()
        val latestWeight = weightRepository.getLatestWeightEntry().first()

        if (meals.isEmpty() && latestWeight == null) return "No nutrition data recorded for this period."

        val byDay = meals.groupBy { it.date }
        val daysLogged = byDay.size

        return buildString {
            if (meals.isNotEmpty()) {
                appendLine("Days with meals logged: $daysLogged/$totalDays")
                if (daysLogged > 0) {
                    val avgCal = byDay.values.map { it.sumOf { m -> m.calories } }.average().toInt()
                    val avgProtein = byDay.values.map { it.sumOf { m -> m.protein } }.average().toInt()
                    val avgCarbs = byDay.values.map { it.sumOf { m -> m.carbs } }.average().toInt()
                    val avgFat = byDay.values.map { it.sumOf { m -> m.fat } }.average().toInt()
                    appendLine("Avg daily calories: $avgCal kcal")
                    appendLine("Avg macros: ${avgProtein}g protein, ${avgCarbs}g carbs, ${avgFat}g fat")
                }
            }
            if (latestWeight != null) {
                appendLine("Latest weight: ${"%.1f".format(latestWeight.weight)} lbs (logged ${latestWeight.date})")
            }
        }.trimEnd()
    }
}
