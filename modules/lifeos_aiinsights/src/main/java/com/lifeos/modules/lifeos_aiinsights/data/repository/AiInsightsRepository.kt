package com.lifeos.modules.lifeos_aiinsights.data.repository

import com.lifeos.core.InsightProvider
import com.lifeos.core.ModuleReport
import com.lifeos.core.ReportDataProvider
import com.lifeos.modules.lifeos_aiinsights.data.local.CachedReportDao
import com.lifeos.modules.lifeos_aiinsights.data.local.CachedReportEntity
import com.lifeos.modules.lifeos_aiinsights.data.remote.GeminiClient
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

sealed class ReportState {
    data object Idle : ReportState()
    data object Loading : ReportState()
    data class Loaded(val content: String, val generatedAt: Long) : ReportState()
    data class Error(val message: String) : ReportState()
}

@Singleton
class AiInsightsRepository @Inject constructor(
    private val reportProviders: Set<@JvmSuppressWildcards ReportDataProvider>,
    private val insightProviders: Set<@JvmSuppressWildcards InsightProvider>,
    private val dao: CachedReportDao,
    private val geminiClient: GeminiClient
) {
    private val currentMonth: String get() = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

    fun getOrderedInsightProviders(): List<InsightProvider> = insightProviders.sortedBy { provider ->
        val order = listOf(
            "weekly_overview", "monthly_overview", "sleep_medication",
            "habit_focus", "mood_journal", "media_next",
            "collection_pick", "retirement_outlook"
        )
        order.indexOf(provider.insightId).let { if (it == -1) order.size else it }
    }

    suspend fun loadCachedInsight(insightId: String): ReportState {
        val entity = dao.getReport(insightId, currentMonth) ?: return ReportState.Idle
        return ReportState.Loaded(entity.content, entity.generatedAt)
    }

    suspend fun generateInsight(insightId: String): ReportState {
        val provider = insightProviders.find { it.insightId == insightId }
            ?: return ReportState.Error("Unknown insight: $insightId")

        val dataContext = try {
            provider.buildDataContext()
        } catch (e: Exception) {
            return ReportState.Error("Failed to collect data: ${e.message}")
        }

        if (dataContext.isBlank()) return ReportState.Error("No data available yet.")

        val prompt = buildInsightPrompt(provider, dataContext)
        return geminiClient.generateInsights(prompt).fold(
            onSuccess = { text ->
                val entity = CachedReportEntity(
                    reportType = insightId,
                    periodStart = currentMonth,
                    generatedAt = System.currentTimeMillis(),
                    content = text
                )
                dao.upsertReport(entity)
                ReportState.Loaded(text, entity.generatedAt)
            },
            onFailure = { e ->
                ReportState.Error(e.message ?: "Failed to generate insight.")
            }
        )
    }

    private fun buildInsightPrompt(provider: InsightProvider, dataContext: String): String = buildString {
        appendLine("You are a personal life assistant helping the user understand their data.")
        appendLine("Answer this specific question: \"${provider.cardQuestion}\"")
        appendLine()
        appendLine("Here is the pre-computed data — do not invent numbers or facts not shown below:")
        appendLine()
        appendLine(dataContext)
        appendLine()
        appendLine("---")
        appendLine("Provide a focused, conversational answer in 150–250 words.")
        append("Use plain text only — no markdown, no asterisks, no bullet symbols. Be specific and actionable.")
    }
}
