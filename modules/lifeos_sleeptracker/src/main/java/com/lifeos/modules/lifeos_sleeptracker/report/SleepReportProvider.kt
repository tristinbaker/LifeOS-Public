package com.lifeos.modules.lifeos_sleeptracker.report

import com.lifeos.core.ModuleReport
import com.lifeos.core.ReportDataProvider
import com.lifeos.modules.lifeos_sleeptracker.data.local.SleepLogEntity
import com.lifeos.modules.lifeos_sleeptracker.data.repository.SleepRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.roundToInt

class SleepReportProvider @Inject constructor(
    private val repository: SleepRepository
) : ReportDataProvider {

    override val sectionName = "sleep"

    override suspend fun getWeeklyReport(startDate: LocalDate, endDate: LocalDate): ModuleReport {
        val logs = repository.getLogsBetween(startDate, endDate)
        val days = startDate.until(endDate).days + 1
        return ModuleReport(
            sectionTitle = "Sleep (${startDate.format(DateTimeFormatter.ofPattern("MMM d"))} – ${endDate.format(DateTimeFormatter.ofPattern("MMM d"))})",
            textBlock = buildSummary(logs, days)
        )
    }

    override suspend fun getMonthlyReport(yearMonth: YearMonth): ModuleReport {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        val logs = repository.getLogsBetween(start, end)
        val days = yearMonth.lengthOfMonth()
        return ModuleReport(
            sectionTitle = "Sleep (${yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))})",
            textBlock = buildSummary(logs, days)
        )
    }

    private fun buildSummary(logs: List<SleepLogEntity>, totalDays: Int): String {
        if (logs.isEmpty()) return "No sleep logs recorded for this period."

        val durations = logs.map { repository.calculateDurationHours(it.startTime, it.endTime).toDouble() }
        val avgHours = durations.average()
        val avgQuality = logs.map { it.quality }.average()
        val medNights = logs.count { it.sleepMedicationTaken }

        val best = logs.maxByOrNull { it.quality }
        val worst = logs.minByOrNull { it.quality }

        val bestStr = best?.let {
            val h = repository.calculateDurationHours(it.startTime, it.endTime).toDouble()
            "${it.date} (${formatHours(h)}, quality ${it.quality}/5.0)"
        } ?: "N/A"

        val worstStr = worst?.let {
            val h = repository.calculateDurationHours(it.startTime, it.endTime).toDouble()
            "${it.date} (${formatHours(h)}, quality ${it.quality}/5.0)"
        } ?: "N/A"

        return buildString {
            appendLine("Nights logged: ${logs.size}/$totalDays")
            appendLine("Avg sleep duration: ${formatHours(avgHours)}")
            appendLine("Avg sleep quality: ${"%.1f".format(avgQuality)}/5.0")
            if (medNights > 0) appendLine("Sleep medication used: $medNights night(s)")
            if (logs.size > 1) {
                appendLine("Best night: $bestStr")
                appendLine("Worst night: $worstStr")
            }
        }.trimEnd()
    }

    private fun formatHours(h: Double): String {
        val hrs = h.toInt()
        val mins = ((h - hrs) * 60).roundToInt()
        return if (mins > 0) "${hrs}h ${mins}m" else "${hrs}h"
    }
}
