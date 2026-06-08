package com.lifeos.modules.lifeos_aiinsights.report

import com.lifeos.core.InsightProvider
import com.lifeos.core.ReportDataProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class WeeklyOverviewInsightProvider @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards ReportDataProvider>
) : InsightProvider {

    override val insightId = "weekly_overview"
    override val cardTitle = "Weekly Overview"
    override val cardQuestion = "How did your week look across sleep, habits, nutrition, and finances?"

    override suspend fun buildDataContext(): String {
        val today = LocalDate.now()
        val weekEnd = today.minusDays(1)
        val weekStart = today.minusDays(7)
        val fmt = DateTimeFormatter.ofPattern("MMM d")

        val reports = providers.mapNotNull { provider ->
            try { provider.getWeeklyReport(weekStart, weekEnd) } catch (e: Exception) { null }
        }

        if (reports.isEmpty()) return "No data available for this week."

        return buildString {
            appendLine("Week of ${weekStart.format(fmt)} – ${weekEnd.format(fmt)}:")
            appendLine()
            reports.forEach { report ->
                appendLine("## ${report.sectionTitle}")
                appendLine(report.textBlock)
                appendLine()
            }
        }.trimEnd()
    }
}
