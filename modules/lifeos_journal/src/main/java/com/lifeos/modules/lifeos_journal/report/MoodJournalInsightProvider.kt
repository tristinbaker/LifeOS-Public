package com.lifeos.modules.lifeos_journal.report

import com.lifeos.core.InsightProvider
import com.lifeos.modules.lifeos_journal.data.local.JournalEntryDao
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MoodJournalInsightProvider @Inject constructor(
    private val journalEntryDao: JournalEntryDao
) : InsightProvider {

    override val insightId = "mood_journal"
    override val cardTitle = "Mood & Journal"
    override val cardQuestion = "Based on what I've written in my journal this week, what themes, patterns, or insights stand out?"

    override suspend fun buildDataContext(): String {
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now()
        val end = today.minusDays(1)
        val start = today.minusDays(7)
        val entries = journalEntryDao.getEntriesBetween(start.format(fmt), end.format(fmt))

        if (entries.isEmpty()) return "No journal entries recorded in the past 7 days."

        val sorted = entries.sortedBy { it.date }
        val avgMood = sorted.map { it.mood }.average()

        val moodLabels = mapOf(1 to "Very Bad", 2 to "Bad", 3 to "Neutral", 4 to "Good", 5 to "Great")
        val moodCounts = sorted.groupBy { it.mood }.mapValues { it.value.size }

        return buildString {
            appendLine("Journal entries — last 7 days excluding today (${entries.size} entries):")
            appendLine("Average mood: ${"%.1f".format(avgMood)}/5 (${moodLabels[avgMood.roundToNearestInt()] ?: "Neutral"})")
            appendLine()
            appendLine("Mood distribution:")
            for (m in 5 downTo 1) {
                val count = moodCounts[m] ?: 0
                if (count > 0) appendLine("  ${moodLabels[m]}: $count day(s)")
            }
            appendLine()
            appendLine("Full journal entries (oldest to newest):")
            sorted.forEach { entry ->
                appendLine("--- ${entry.date} (mood ${entry.mood}/5: ${moodLabels[entry.mood] ?: "Unknown"}) ---")
                appendLine(entry.content)
                appendLine()
            }
        }.trimEnd()
    }

    private fun Double.roundToNearestInt(): Int = Math.round(this).toInt().coerceIn(1, 5)
}
