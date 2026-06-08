package com.lifeos.modules.lifeos_sleeptracker.report

import com.lifeos.core.InsightProvider
import com.lifeos.modules.lifeos_sleeptracker.data.repository.SleepRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

class SleepMedicationInsightProvider @Inject constructor(
    private val repository: SleepRepository
) : InsightProvider {

    override val insightId = "sleep_medication"
    override val cardTitle = "Sleep & Medication"
    override val cardQuestion = "How does sleep medication affect your sleep quality and duration?"

    override suspend fun buildDataContext(): String {
        val end = LocalDate.now()
        val start = end.minusDays(89)
        val logs = repository.getLogsBetween(start, end)

        if (logs.isEmpty()) return "No sleep logs recorded in the past 90 days."

        val withMed = logs.filter { it.sleepMedicationTaken }
        val withoutMed = logs.filter { !it.sleepMedicationTaken }

        return buildString {
            appendLine("Sleep data — last 90 days (${logs.size} nights logged):")
            appendLine()

            if (withMed.isNotEmpty()) {
                val avgQuality = withMed.map { it.quality }.average()
                val avgDuration = withMed.map { repository.calculateDurationHours(it.startTime, it.endTime).toDouble() }.average()
                appendLine("Nights WITH sleep medication: ${withMed.size}")
                appendLine("  Avg quality: ${"%.1f".format(avgQuality)}/5.0")
                appendLine("  Avg duration: ${formatHours(avgDuration)}")
            } else {
                appendLine("No nights with sleep medication recorded.")
            }

            appendLine()

            if (withoutMed.isNotEmpty()) {
                val avgQuality = withoutMed.map { it.quality }.average()
                val avgDuration = withoutMed.map { repository.calculateDurationHours(it.startTime, it.endTime).toDouble() }.average()
                appendLine("Nights WITHOUT sleep medication: ${withoutMed.size}")
                appendLine("  Avg quality: ${"%.1f".format(avgQuality)}/5.0")
                appendLine("  Avg duration: ${formatHours(avgDuration)}")
            } else {
                appendLine("All recorded nights used sleep medication.")
            }

            if (withMed.isNotEmpty() && withoutMed.isNotEmpty()) {
                appendLine()
                val qualityDiff = withMed.map { it.quality }.average() - withoutMed.map { it.quality }.average()
                val durationDiff = withMed.map { repository.calculateDurationHours(it.startTime, it.endTime).toDouble() }.average() -
                        withoutMed.map { repository.calculateDurationHours(it.startTime, it.endTime).toDouble() }.average()
                appendLine("Quality difference (with vs without): ${if (qualityDiff >= 0) "+" else ""}${"%.1f".format(qualityDiff)}")
                appendLine("Duration difference: ${if (durationDiff >= 0) "+" else ""}${formatHours(durationDiff)}")
            }
        }.trimEnd()
    }

    private fun formatHours(h: Double): String {
        val absH = Math.abs(h)
        val hrs = absH.toInt()
        val mins = ((absH - hrs) * 60).roundToInt()
        val sign = if (h < 0) "-" else ""
        return if (mins > 0) "${sign}${hrs}h ${mins}m" else "${sign}${hrs}h"
    }
}
