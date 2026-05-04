package com.lifeos.modules.lifeos_aiinsights.data.repository

import com.lifeos.core.ModuleReport
import com.lifeos.core.ReportDataProvider
import com.lifeos.modules.lifeos_aiinsights.data.local.CachedReportDao
import com.lifeos.modules.lifeos_aiinsights.data.local.CachedReportEntity
import com.lifeos.modules.lifeos_aiinsights.data.remote.GeminiClient
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

enum class ReportType { WEEKLY, MONTHLY }

sealed class ReportState {
    data object Idle : ReportState()
    data object Loading : ReportState()
    data class Loaded(val content: String, val generatedAt: Long) : ReportState()
    data class Error(val message: String) : ReportState()
}

@Singleton
class AiInsightsRepository @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards ReportDataProvider>,
    private val dao: CachedReportDao,
    private val geminiClient: GeminiClient
) {
    suspend fun loadCached(type: ReportType, periodStart: String): ReportState {
        val entity = dao.getReport(type.name, periodStart) ?: return ReportState.Idle
        return ReportState.Loaded(entity.content, entity.generatedAt)
    }

    suspend fun generateReport(type: ReportType, periodStart: LocalDate): ReportState {
        val reports = collectReports(type, periodStart)
        if (reports.isEmpty()) return ReportState.Error("No data available to analyze yet.")

        val period = when (type) {
            ReportType.WEEKLY -> {
                val end = periodStart.plusDays(6)
                "${periodStart.format(DateTimeFormatter.ofPattern("MMM d"))} – ${end.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
            }
            ReportType.MONTHLY -> YearMonth.from(periodStart).format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        }

        val prompt = buildPrompt(reports, type, period)
        return geminiClient.generateInsights(prompt).fold(
            onSuccess = { text ->
                val entity = CachedReportEntity(
                    reportType = type.name,
                    periodStart = periodStart.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    generatedAt = System.currentTimeMillis(),
                    content = text
                )
                dao.upsertReport(entity)
                ReportState.Loaded(text, entity.generatedAt)
            },
            onFailure = { e ->
                ReportState.Error(e.message ?: "Failed to generate insights.")
            }
        )
    }

    private suspend fun collectReports(type: ReportType, periodStart: LocalDate): List<ModuleReport> {
        return providers.mapNotNull { provider ->
            try {
                when (type) {
                    ReportType.WEEKLY -> provider.getWeeklyReport(periodStart, periodStart.plusDays(6))
                    ReportType.MONTHLY -> provider.getMonthlyReport(YearMonth.from(periodStart))
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun buildPrompt(reports: List<ModuleReport>, type: ReportType, period: String): String {
        val periodLabel = if (type == ReportType.WEEKLY) "week" else "month"
        return buildString {
            appendLine("You are a personal life coach assistant.")
            appendLine("The user tracks their sleep, habits, nutrition, and finances.")
            appendLine("Below is a pre-computed summary of their data for: $period")
            appendLine("Do not invent numbers or facts — use only what is provided below.")
            appendLine()
            reports.forEach { report ->
                appendLine("## ${report.sectionTitle}")
                appendLine(report.textBlock)
                appendLine()
            }
            appendLine("---")
            appendLine("Please provide:")
            appendLine("1. A 2–3 sentence overall summary of how this $periodLabel went.")
            appendLine("2. 2–3 cross-domain observations (e.g. how sleep affected habit completion, or how spending correlated with activity).")
            appendLine("3. Two specific, actionable recommendations for next $periodLabel.")
            appendLine("4. One thing they should feel good about.")
            appendLine()
            append("Keep your response under 300 words. Use plain text only — no markdown, no asterisks, no bullet symbols.")
        }
    }
}
