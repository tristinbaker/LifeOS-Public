package com.lifeos.modules.lifeos_habittracker.report

import com.lifeos.core.InsightProvider
import com.lifeos.modules.lifeos_habittracker.data.local.HabitDao
import com.lifeos.modules.lifeos_habittracker.data.repository.HabitsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class HabitFocusInsightProvider @Inject constructor(
    private val repository: HabitsRepository,
    private val habitDao: HabitDao
) : InsightProvider {

    override val insightId = "habit_focus"
    override val cardTitle = "Habit Focus"
    override val cardQuestion = "Which habits need your attention and which are thriving?"

    override suspend fun buildDataContext(): String {
        val habits = repository.getAllHabits().first().filter { !it.isArchived }
        if (habits.isEmpty()) return "No active habits tracked."

        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now()
        val rangeEnd = today.minusDays(1)
        val rangeStart = today.minusDays(30)
        val startStr = rangeStart.format(fmt)
        val endStr = rangeEnd.format(fmt)

        data class HabitStat(val name: String, val checkIns: Int, val pct: Int, val streak: Int)

        val stats = habits.map { habit ->
            val checkIns = habitDao.getCheckInsBetween(habit.id, startStr, endStr).size
            val pct = (checkIns * 100 / 30).coerceAtMost(100)
            val streak = repository.getStreak(habit.id)
            HabitStat(habit.name, checkIns, pct, streak)
        }

        val sorted = stats.sortedBy { it.pct }
        val needsAttention = sorted.take(3)
        val topPerformer = stats.maxByOrNull { it.pct }
        val longestStreak = stats.maxByOrNull { it.streak }

        return buildString {
            appendLine("Habit performance — last 30 days excluding today (${habits.size} active habits):")
            appendLine()
            appendLine("Needs attention (lowest completion):")
            needsAttention.forEach { s ->
                appendLine("  ${s.name}: ${s.checkIns}/30 days (${s.pct}%) — current streak: ${s.streak} day(s)")
            }
            appendLine()
            if (topPerformer != null) {
                appendLine("Best performing: ${topPerformer.name} — ${topPerformer.pct}% completion")
            }
            if (longestStreak != null && longestStreak.streak > 0) {
                appendLine("Longest current streak: ${longestStreak.name} — ${longestStreak.streak} day(s)")
            }
            appendLine()
            appendLine("All habits:")
            stats.sortedByDescending { it.pct }.forEach { s ->
                appendLine("  ${s.name}: ${s.pct}% (streak: ${s.streak} days)")
            }
        }.trimEnd()
    }
}
