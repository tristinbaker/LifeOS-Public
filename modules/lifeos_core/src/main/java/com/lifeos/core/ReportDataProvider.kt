package com.lifeos.core

import java.time.LocalDate
import java.time.YearMonth

data class ModuleReport(val sectionTitle: String, val textBlock: String)

interface ReportDataProvider {
    val sectionName: String
    suspend fun getWeeklyReport(startDate: LocalDate, endDate: LocalDate): ModuleReport
    suspend fun getMonthlyReport(yearMonth: YearMonth): ModuleReport
}
