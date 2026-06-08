package com.lifeos.modules.lifeos_aiinsights.report

import com.lifeos.core.InsightProvider
import com.lifeos.core.ReportDataProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MonthlyOverviewInsightProvider @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards ReportDataProvider>
) : InsightProvider {

    override val insightId = "monthly_overview"
    override val cardTitle = "Monthly Overview"
    override val cardQuestion = "How did your last 30 days look across all areas of your life?"

    override suspend fun buildDataContext(): String {
        val today = LocalDate.now()
        val endDate = today.minusDays(1)
        val startDate = today.minusDays(30)
        val fmt = DateTimeFormatter.ofPattern("MMM d")
        val label = "${startDate.format(fmt)} – ${endDate.format(fmt)}"

        val reports = providers.mapNotNull { provider ->
            try { provider.getWeeklyReport(startDate, endDate) } catch (e: Exception) { null }
        }

        if (reports.isEmpty()) return "No data available for the last 30 days."

        return buildString {
            appendLine("Last 30 days ($label) summary:")
            appendLine()
            reports.forEach { report ->
                appendLine("## ${report.sectionTitle}")
                appendLine(report.textBlock)
                appendLine()
            }
        }.trimEnd()
    }
}
