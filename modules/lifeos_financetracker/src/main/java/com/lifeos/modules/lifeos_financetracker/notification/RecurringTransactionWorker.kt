package com.lifeos.modules.lifeos_financetracker.notification

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lifeos.modules.lifeos_financetracker.data.local.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

class RecurringTransactionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = Room.databaseBuilder(applicationContext, FinanceDatabase::class.java, "lifeos_financetracker.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()

        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val recurringDao = db.recurringTransactionDao()
        val transactionDao = db.transactionDao()
        val netWorthDao = db.netWorthHistoryDao()
        val accountDao = db.accountDao()
        val snapshotDao = db.accountSnapshotDao()
        val mortgageDao = db.mortgageDetailsDao()

        val due = recurringDao.getDueRecurring(todayStr)
        for (r in due) {
            transactionDao.insert(
                TransactionEntity(
                    amount = r.amount,
                    type = r.type,
                    categoryId = r.categoryId,
                    accountId = r.accountId,
                    toAccountId = r.toAccountId,
                    date = todayStr,
                    note = r.label
                )
            )
            if (r.type == TransactionType.TRANSFER && r.toAccountId != null) {
                mortgageDao.getForAccount(r.toAccountId)?.let { details ->
                    val advanced = computeMortgageBalance(details)
                    mortgageDao.upsert(details.copy(
                        currentBalance = maxOf(0.0, advanced - r.amount),
                        asOfDate = todayStr
                    ))
                }
            }
            recurringDao.update(r.copy(nextPostDate = computeNextDate(r, today)))
        }

        // Process sinking fund auto-contributions
        val sinkingFundDao = db.sinkingFundDao()
        val contributionDao = db.sinkingFundContributionDao()
        val currentMonthPrefix = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        sinkingFundDao.getDueToday(today.dayOfMonth).forEach { fund ->
            val alreadyContributedThisMonth = contributionDao.getForFundInMonth(fund.id, currentMonthPrefix).isNotEmpty()
            if (!alreadyContributedThisMonth) {
                val totalContributed = contributionDao.getTotalContributedCents(fund.id)
                val currentSaved = fund.initialBalanceCents + totalContributed
                val targetYearMonth = try {
                    YearMonth.from(LocalDate.parse(fund.targetDate))
                } catch (e: Exception) { YearMonth.now().plusMonths(1) }
                val monthsRemaining = (ChronoUnit.MONTHS.between(YearMonth.now(), targetYearMonth).toInt() + 1).coerceAtLeast(1)
                val remaining = (fund.targetAmountCents - currentSaved).coerceAtLeast(0L)
                if (remaining > 0) {
                    val contributionAmount = ceil(remaining.toDouble() / monthsRemaining).toLong()
                    contributionDao.insert(
                        SinkingFundContributionEntity(
                            fundId = fund.id,
                            amountCents = contributionAmount,
                            date = todayStr
                        )
                    )
                    if (fund.accountId != null) {
                        transactionDao.insert(
                            TransactionEntity(
                                amount = contributionAmount / 100.0,
                                type = TransactionType.EXPENSE,
                                categoryId = fund.categoryId ?: 0L,
                                accountId = fund.accountId,
                                date = todayStr,
                                note = fund.name
                            )
                        )
                    }
                }
            }
        }

        recordNetWorthSnapshot(db, today, netWorthDao, accountDao, transactionDao, snapshotDao, mortgageDao)

        db.close()
        return Result.success()
    }

    private suspend fun recordNetWorthSnapshot(
        db: FinanceDatabase,
        today: LocalDate,
        netWorthDao: NetWorthHistoryDao,
        accountDao: AccountDao,
        transactionDao: TransactionDao,
        snapshotDao: AccountSnapshotDao,
        mortgageDao: MortgageDetailsDao
    ) {
        // Simple sync queries for worker context
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        // We skip the snapshot here since Flow isn't suitable for worker context
        // The ViewModel records the snapshot reactively when it first collects
        netWorthDao.upsert(
            NetWorthSnapshotEntity(
                date = todayStr,
                totalAssets = 0.0,
                totalLiabilities = 0.0,
                netWorth = 0.0
            )
        )
    }

    private fun computeMortgageBalance(details: MortgageDetailsEntity): Double {
        val r = details.aprPercent / 100.0 / 12.0
        val n = ChronoUnit.MONTHS.between(LocalDate.parse(details.asOfDate), LocalDate.now()).toInt().coerceAtLeast(0)
        if (n == 0) return details.currentBalance
        if (r == 0.0) return maxOf(0.0, details.currentBalance - details.monthlyPayment * n)
        val factor = Math.pow(1 + r, n.toDouble())
        return maxOf(0.0, details.currentBalance * factor - details.monthlyPayment * (factor - 1) / r)
    }

    private fun computeNextDate(r: RecurringTransactionEntity, from: LocalDate): String {
        val next = when (r.frequency) {
            RecurringFrequency.DAILY -> from.plusDays(1)
            RecurringFrequency.WEEKLY -> from.plusWeeks(1)
            RecurringFrequency.BIWEEKLY -> from.plusWeeks(2)
            RecurringFrequency.MONTHLY -> {
                val day = r.dayOfMonth ?: 1
                from.plusMonths(1).withDayOfMonth(minOf(day, from.plusMonths(1).lengthOfMonth()))
            }
            RecurringFrequency.QUARTERLY -> {
                val day = r.dayOfMonth ?: 1
                from.plusMonths(3).withDayOfMonth(minOf(day, from.plusMonths(3).lengthOfMonth()))
            }
            RecurringFrequency.SEMI_ANNUAL -> {
                val day = r.dayOfMonth ?: 1
                from.plusMonths(6).withDayOfMonth(minOf(day, from.plusMonths(6).lengthOfMonth()))
            }
            RecurringFrequency.YEARLY -> {
                val day = r.dayOfMonth ?: 1
                from.plusYears(1).withDayOfMonth(minOf(day, from.plusYears(1).lengthOfMonth()))
            }
        }
        return next.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
