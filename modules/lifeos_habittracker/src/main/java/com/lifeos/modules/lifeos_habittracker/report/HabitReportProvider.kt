package com.lifeos.modules.lifeos_habittracker.report

import com.lifeos.core.ModuleReport
import com.lifeos.core.ReportDataProvider
import com.lifeos.modules.lifeos_habittracker.data.local.HabitDao
import com.lifeos.modules.lifeos_habittracker.data.repository.HabitsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class HabitReportProvider @Inject constructor(
    private val repository: HabitsRepository,
    private val habitDao: HabitDao
) : ReportDataProvider {

    override val sectionName = "habits"

    override suspend fun getWeeklyReport(startDate: LocalDate, endDate: LocalDate): ModuleReport {
        val days = startDate.until(endDate).days + 1
        return ModuleReport(
            sectionTitle = "Habits (${startDate.format(DateTimeFormatter.ofPattern("MMM d"))} – ${endDate.format(DateTimeFormatter.ofPattern("MMM d"))})",
            textBlock = buildSummary(startDate, endDate, days)
        )
    }

    override suspend fun getMonthlyReport(yearMonth: YearMonth): ModuleReport {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        val days = yearMonth.lengthOfMonth()
        return ModuleReport(
            sectionTitle = "Habits (${yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))})",
            textBlock = buildSummary(start, end, days)
        )
    }

    private suspend fun buildSummary(startDate: LocalDate, endDate: LocalDate, totalDays: Int): String {
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val startStr = startDate.format(fmt)
        val endStr = endDate.format(fmt)

        val habits = repository.getAllHabits().first().filter { !it.isArchived }
        if (habits.isEmpty()) return "No active habits tracked for this period."

        data class HabitStat(val name: String, val checkIns: Int, val streak: Int)

        val stats = habits.map { habit ->
            val checkIns = habitDao.getCheckInsBetween(habit.id, startStr, endStr).size
            val streak = repository.getStreak(habit.id)
            HabitStat(habit.name, checkIns, streak)
        }.sortedByDescending { it.checkIns.toDouble() / totalDays }

        val topStreak = stats.maxByOrNull { it.streak }

        return buildString {
            appendLine("Active habits: ${habits.size}")
            if (topStreak != null && topStreak.streak > 0) {
                appendLine("Top streak: \"${topStreak.name}\" – ${topStreak.streak} day(s)")
            }
            appendLine("Completion this period:")
            stats.forEach { s ->
                val pct = (s.checkIns.toDouble() / totalDays * 100).toInt()
                appendLine("  ${s.name}: ${s.checkIns}/$totalDays ($pct%)")
            }
        }.trimEnd()
    }
}
