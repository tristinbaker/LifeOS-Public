package com.lifeos.modules.lifeos_financetracker.report

import com.lifeos.core.InsightProvider
import com.lifeos.core.UserPrefs
import com.lifeos.modules.lifeos_financetracker.data.local.AccountDao
import com.lifeos.modules.lifeos_financetracker.data.local.AccountSnapshotDao
import com.lifeos.modules.lifeos_financetracker.data.local.AccountType
import com.lifeos.modules.lifeos_financetracker.data.local.NetWorthHistoryDao
import com.lifeos.modules.lifeos_financetracker.data.local.SinkingFundDao
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import kotlin.math.pow
import javax.inject.Inject

class RetirementOutlookInsightProvider @Inject constructor(
    private val netWorthDao: NetWorthHistoryDao,
    private val sinkingFundDao: SinkingFundDao,
    private val accountDao: AccountDao,
    private val accountSnapshotDao: AccountSnapshotDao,
    private val userPrefs: UserPrefs
) : InsightProvider {

    override val insightId = "retirement_outlook"
    override val cardTitle = "Retirement Outlook"
    override val cardQuestion = "How does your retirement situation look at your current age?"

    override suspend fun buildDataContext(): String {
        val birthYear = userPrefs.birthYear
        val currentYear = LocalDate.now().year
        val age = currentYear - birthYear
        val yearsToRetirement = (65 - age).coerceAtLeast(0)

        val snapshots = netWorthDao.getRecentHistory(24).first()
            .sortedBy { it.date }
        val latestSnapshot = snapshots.lastOrNull()
        val yearAgoSnapshot = if (snapshots.size >= 12) snapshots[snapshots.size - 12] else snapshots.firstOrNull()

        val allAccounts = accountDao.getAllAccounts().first()
        val investmentAccounts = allAccounts.filter { it.type == AccountType.INVESTMENT }
        val allSnapshots = accountSnapshotDao.getAllSnapshots().first()

        data class InvestmentAccountBalance(val name: String, val balance: Double)
        val investmentBalances = investmentAccounts.map { account ->
            val latestBalance = allSnapshots
                .filter { it.accountId == account.id }
                .maxByOrNull { it.date }
                ?.balance ?: account.startingBalance
            InvestmentAccountBalance(account.name, latestBalance)
        }
        val totalInvestments = investmentBalances.sumOf { it.balance }

        val sinkingFunds = sinkingFundDao.getAllActive().first()

        return buildString {
            appendLine("Retirement & financial outlook:")
            appendLine("Current age: $age years old (born $birthYear)")
            appendLine("Years to traditional retirement (age 65): $yearsToRetirement")
            appendLine()

            if (latestSnapshot != null && latestSnapshot.netWorth != 0.0) {
                appendLine("Current net worth: ${formatMoney(latestSnapshot.netWorth)}")
                appendLine("  Total assets: ${formatMoney(latestSnapshot.totalAssets)}")
                appendLine("  Total liabilities: ${formatMoney(latestSnapshot.totalLiabilities)}")

                if (yearAgoSnapshot != null && yearAgoSnapshot.date != latestSnapshot.date) {
                    val netWorthGrowth = latestSnapshot.netWorth - yearAgoSnapshot.netWorth
                    appendLine("Net worth change (past ~12 months): ${if (netWorthGrowth >= 0) "+" else ""}${formatMoney(netWorthGrowth)}")
                }
            } else {
                appendLine("Net worth data not yet available. Log account balances to see projections.")
            }

            if (investmentBalances.isNotEmpty()) {
                appendLine()
                appendLine("Investment accounts (${investmentBalances.size} accounts, total: ${formatMoney(totalInvestments)}):")
                investmentBalances.forEach { inv ->
                    appendLine("  ${inv.name}: ${formatMoney(inv.balance)}")
                }

                if (totalInvestments > 0 && yearsToRetirement > 0) {
                    val conservativeProjection = totalInvestments * (1.06).pow(yearsToRetirement.toDouble())
                    val moderateProjection = totalInvestments * (1.07).pow(yearsToRetirement.toDouble())
                    val optimisticProjection = totalInvestments * (1.08).pow(yearsToRetirement.toDouble())
                    appendLine()
                    appendLine("Compound growth projections (investment accounts only, excludes liabilities):")
                    appendLine("  Conservative (6%/yr): ${formatMoney(conservativeProjection)}")
                    appendLine("  Moderate (7%/yr):     ${formatMoney(moderateProjection)}")
                    appendLine("  Optimistic (8%/yr):   ${formatMoney(optimisticProjection)}")
                }
            } else {
                appendLine()
                appendLine("No investment accounts found. Add investment accounts (401k, Roth IRA, brokerage) to see retirement projections.")
            }

            if (sinkingFunds.isNotEmpty()) {
                appendLine()
                appendLine("Active savings goals: ${sinkingFunds.size}")
                sinkingFunds.take(5).forEach { fund ->
                    appendLine("  ${fund.name}: target \$${fund.targetAmountCents / 100} by ${fund.targetDate}")
                }
            }

            appendLine()
            appendLine("The user is $age years old with $yearsToRetirement years until traditional retirement. Analyze their current trajectory and provide specific, actionable retirement guidance including savings rate recommendations and realistic expectations.")
        }.trimEnd()
    }

    private fun formatMoney(amount: Double): String = "$${"%,.2f".format(amount)}"
}
