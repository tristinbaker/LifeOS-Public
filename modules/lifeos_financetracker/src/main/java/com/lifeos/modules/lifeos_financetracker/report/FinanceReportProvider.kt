package com.lifeos.modules.lifeos_financetracker.report

import com.lifeos.core.ModuleReport
import com.lifeos.core.ReportDataProvider
import com.lifeos.modules.lifeos_financetracker.data.local.TransactionType
import com.lifeos.modules.lifeos_financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class FinanceReportProvider @Inject constructor(
    private val repository: FinanceRepository
) : ReportDataProvider {

    override val sectionName = "finance"

    override suspend fun getWeeklyReport(startDate: LocalDate, endDate: LocalDate): ModuleReport {
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val all = repository.getAllTransactions().first()
        val txns = all.filter {
            val d = try { LocalDate.parse(it.date, fmt) } catch (e: Exception) { return@filter false }
            !d.isBefore(startDate) && !d.isAfter(endDate)
        }

        val income = txns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenses = txns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val categories = repository.getAllCategories().first()
        val byCategory = txns.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { (_, v) -> v.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }.take(3)

        return ModuleReport(
            sectionTitle = "Finances (${startDate.format(DateTimeFormatter.ofPattern("MMM d"))} – ${endDate.format(DateTimeFormatter.ofPattern("MMM d"))})",
            textBlock = buildString {
                appendLine("Income: ${formatMoney(income)}")
                appendLine("Expenses: ${formatMoney(expenses)}")
                appendLine("Net: ${formatMoney(income - expenses)}")
                if (byCategory.isNotEmpty()) {
                    appendLine("Top expense categories:")
                    byCategory.forEach { (catId, total) ->
                        val name = categories.find { it.id == catId }?.name ?: "Other"
                        appendLine("  $name: ${formatMoney(total)}")
                    }
                }
            }.trimEnd()
        )
    }

    override suspend fun getMonthlyReport(yearMonth: YearMonth): ModuleReport {
        val txns = repository.getTransactionsForMonth(yearMonth).first()
        val categories = repository.getAllCategories().first()
        val budgets = repository.getAllBudgets().first()

        val income = txns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenses = txns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val byCategory = txns.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { (_, v) -> v.sumOf { it.amount } }

        val topCategories = byCategory.entries.sortedByDescending { it.value }.take(3)

        val overBudget = budgets.filter { b ->
            val spent = byCategory[b.categoryId] ?: 0.0
            val limit = b.monthlyLimitCents / 100.0
            spent > limit
        }.mapNotNull { b ->
            val name = categories.find { it.id == b.categoryId }?.name ?: return@mapNotNull null
            val spent = byCategory[b.categoryId] ?: 0.0
            val limit = b.monthlyLimitCents / 100.0
            "$name: spent ${formatMoney(spent)} vs budget ${formatMoney(limit)}"
        }

        return ModuleReport(
            sectionTitle = "Finances (${yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))})",
            textBlock = buildString {
                appendLine("Income: ${formatMoney(income)}")
                appendLine("Expenses: ${formatMoney(expenses)}")
                appendLine("Net savings: ${formatMoney(income - expenses)}")
                if (topCategories.isNotEmpty()) {
                    appendLine("Top expense categories:")
                    topCategories.forEach { (catId, total) ->
                        val name = categories.find { it.id == catId }?.name ?: "Other"
                        appendLine("  $name: ${formatMoney(total)}")
                    }
                }
                if (overBudget.isNotEmpty()) {
                    appendLine("Over-budget categories:")
                    overBudget.forEach { appendLine("  $it") }
                }
            }.trimEnd()
        )
    }

    private fun formatMoney(amount: Double): String = "${"$"}${"%.2f".format(amount)}"
}
