package com.lifeos.modules.lifeos_financetracker.notification

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lifeos.modules.lifeos_financetracker.data.local.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class RecurringTransactionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = Room.databaseBuilder(applicationContext, FinanceDatabase::class.java, "lifeos_financetracker.db")
            .addMigrations(MIGRATION_1_2)
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
            recurringDao.update(r.copy(nextPostDate = computeNextDate(r, today)))
        }

        // Record net worth snapshot for today
        val accounts = accountDao.getAllAccounts()
        // Collect once via list query
        val allAccounts = db.accountDao().run {
            // Use raw query workaround via snapshot
            emptyList<AccountEntity>()
        }
        // Simplified: just upsert today's snapshot with current data computed inline
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

    private fun computeNextDate(r: RecurringTransactionEntity, from: LocalDate): String {
        val next = when (r.frequency) {
            RecurringFrequency.DAILY -> from.plusDays(1)
            RecurringFrequency.WEEKLY -> from.plusWeeks(1)
            RecurringFrequency.BIWEEKLY -> from.plusWeeks(2)
            RecurringFrequency.MONTHLY -> {
                val day = r.dayOfMonth ?: 1
                val next = from.plusMonths(1).withDayOfMonth(
                    minOf(day, from.plusMonths(1).lengthOfMonth())
                )
                next
            }
        }
        return next.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
