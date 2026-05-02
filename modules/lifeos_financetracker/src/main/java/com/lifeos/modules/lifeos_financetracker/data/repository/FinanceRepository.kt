package com.lifeos.modules.lifeos_financetracker.data.repository

import com.lifeos.modules.lifeos_financetracker.data.local.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

data class CategorySpending(
    val category: CategoryEntity,
    val totalSpent: Double,
    val budgetLimitCents: Long?
)

data class AccountWithBalance(
    val account: AccountEntity,
    val balance: Double,
    val mortgageDetails: MortgageDetailsEntity? = null
)

@Singleton
class FinanceRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val accountDao: AccountDao,
    private val accountSnapshotDao: AccountSnapshotDao,
    private val mortgageDetailsDao: MortgageDetailsDao,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val netWorthHistoryDao: NetWorthHistoryDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val seedScope = CoroutineScope(Dispatchers.IO)

    init {
        seedScope.launch { seedDefaultCategoriesIfEmpty() }
    }

    // ---- Balance computation helpers ----

    fun computeTransactionBalance(account: AccountEntity, allTxns: List<TransactionEntity>): Double {
        var balance = account.startingBalance
        allTxns.filter { it.accountId == account.id || it.toAccountId == account.id }.forEach { tx ->
            when {
                tx.accountId == account.id -> when (tx.type) {
                    TransactionType.INCOME -> if (account.type.isAsset()) balance += tx.amount else balance -= tx.amount
                    TransactionType.EXPENSE -> if (account.type.isAsset()) balance -= tx.amount else balance += tx.amount
                    TransactionType.TRANSFER -> balance -= tx.amount
                }
                tx.toAccountId == account.id && tx.type == TransactionType.TRANSFER ->
                    balance -= tx.amount
            }
        }
        return balance
    }

    fun computeMortgageBalance(details: MortgageDetailsEntity): Double {
        val r = details.aprPercent / 100.0 / 12.0
        val n = ChronoUnit.MONTHS.between(LocalDate.parse(details.asOfDate), LocalDate.now())
            .toInt().coerceAtLeast(0)
        if (n == 0) return details.currentBalance
        if (r == 0.0) return maxOf(0.0, details.currentBalance - details.monthlyPayment * n)
        val factor = (1 + r).pow(n.toDouble())
        return maxOf(0.0, details.currentBalance * factor - details.monthlyPayment * (factor - 1) / r)
    }

    // ---- Accounts ----

    fun getAllAccountsWithBalance(): Flow<List<AccountWithBalance>> = combine(
        accountDao.getAllAccounts(),
        transactionDao.getAllTransactions(),
        accountSnapshotDao.getAllSnapshots(),
        mortgageDetailsDao.getAllMortgageDetails()
    ) { accounts, txns, snapshots, mortgages ->
        val latestSnapshot = snapshots.groupBy { it.accountId }
            .mapValues { (_, list) -> list.maxByOrNull { it.date } }
        val mortgageMap = mortgages.associateBy { it.accountId }
        accounts.map { account ->
            val balance = when {
                account.type.isTransactionDriven() -> computeTransactionBalance(account, txns)
                account.type.isSnapshot() -> latestSnapshot[account.id]?.balance ?: account.startingBalance
                account.type == AccountType.MORTGAGE -> mortgageMap[account.id]?.let { computeMortgageBalance(it) } ?: 0.0
                else -> account.startingBalance
            }
            AccountWithBalance(account, balance, mortgageMap[account.id])
        }
    }

    suspend fun insertAccount(account: AccountEntity): Long = accountDao.insert(account)
    suspend fun updateAccount(account: AccountEntity) = accountDao.update(account)
    suspend fun softDeleteAccount(id: Long) = accountDao.softDelete(id)
    suspend fun getAccountById(id: Long): AccountEntity? = accountDao.getById(id)

    // ---- Snapshots ----

    fun getSnapshotsForAccount(accountId: Long): Flow<List<AccountSnapshotEntity>> =
        accountSnapshotDao.getSnapshotsForAccount(accountId)

    suspend fun insertSnapshot(snapshot: AccountSnapshotEntity): Long =
        accountSnapshotDao.insert(snapshot)

    // ---- Mortgage ----

    suspend fun upsertMortgageDetails(details: MortgageDetailsEntity) =
        mortgageDetailsDao.upsert(details)

    suspend fun deleteMortgageDetails(accountId: Long) =
        mortgageDetailsDao.deleteForAccount(accountId)

    suspend fun getMortgageDetails(accountId: Long): MortgageDetailsEntity? =
        mortgageDetailsDao.getForAccount(accountId)

    // ---- Transactions ----

    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsForMonth(month: YearMonth): Flow<List<TransactionEntity>> {
        val start = month.atDay(1).format(dateFormatter)
        val end = month.atEndOfMonth().format(dateFormatter)
        return transactionDao.getTransactionsForMonth(start, end)
    }

    fun getTransactionsForAccount(accountId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForAccount(accountId)

    suspend fun insertTransaction(tx: TransactionEntity): Long = transactionDao.insert(tx)
    suspend fun updateTransaction(tx: TransactionEntity) = transactionDao.update(tx)
    suspend fun deleteTransaction(id: Long) = transactionDao.deleteById(id)
    suspend fun getTransactionById(id: Long): TransactionEntity? = transactionDao.getById(id)

    // ---- Categories ----

    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insert(category)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.update(category)
    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.delete(category)
        budgetDao.deleteForCategory(category.id)
    }

    // ---- Budgets ----

    fun getAllBudgets(): Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    suspend fun upsertBudget(budget: BudgetEntity) = budgetDao.upsert(budget)
    suspend fun deleteBudgetForCategory(categoryId: Long) = budgetDao.deleteForCategory(categoryId)

    // ---- Recurring ----

    fun getActiveRecurring(): Flow<List<RecurringTransactionEntity>> =
        recurringTransactionDao.getActiveRecurring()

    suspend fun getDueRecurring(date: String): List<RecurringTransactionEntity> =
        recurringTransactionDao.getDueRecurring(date)

    suspend fun insertRecurring(recurring: RecurringTransactionEntity): Long =
        recurringTransactionDao.insert(recurring)

    suspend fun updateRecurring(recurring: RecurringTransactionEntity) =
        recurringTransactionDao.update(recurring)

    suspend fun deleteRecurring(id: Long) = recurringTransactionDao.deleteById(id)

    suspend fun setRecurringActive(id: Long, isActive: Boolean) =
        recurringTransactionDao.setActive(id, isActive)

    suspend fun getRecurringById(id: Long): RecurringTransactionEntity? =
        recurringTransactionDao.getById(id)

    // ---- Net worth history ----

    fun getNetWorthHistory(): Flow<List<NetWorthSnapshotEntity>> =
        netWorthHistoryDao.getRecentHistory(90)

    suspend fun recordNetWorthSnapshot(assets: Double, liabilities: Double) {
        netWorthHistoryDao.upsert(
            NetWorthSnapshotEntity(
                date = LocalDate.now().format(dateFormatter),
                totalAssets = assets,
                totalLiabilities = liabilities,
                netWorth = assets - liabilities
            )
        )
    }

    // ---- Category seeding ----

    private suspend fun seedDefaultCategoriesIfEmpty() {
        if (categoryDao.getCount() > 0) return
        categoryDao.insertAll(defaultCategories())
    }

    private fun defaultCategories(): List<CategoryEntity> = listOf(
        CategoryEntity(name = "Food & Dining",   iconName = "restaurant",         colorHex = "#FF5722", isDefault = true, sortOrder = 0),
        CategoryEntity(name = "Housing",          iconName = "home",               colorHex = "#5C6BC0", isDefault = true, sortOrder = 1),
        CategoryEntity(name = "Transport",        iconName = "directions_car",     colorHex = "#26A69A", isDefault = true, sortOrder = 2),
        CategoryEntity(name = "Entertainment",    iconName = "movie",              colorHex = "#AB47BC", isDefault = true, sortOrder = 3),
        CategoryEntity(name = "Health",           iconName = "health_and_safety",  colorHex = "#EF5350", isDefault = true, sortOrder = 4),
        CategoryEntity(name = "Shopping",         iconName = "shopping_bag",       colorHex = "#FFA726", isDefault = true, sortOrder = 5),
        CategoryEntity(name = "Utilities",        iconName = "bolt",               colorHex = "#78909C", isDefault = true, sortOrder = 6),
        CategoryEntity(name = "Mortgage",         iconName = "home",               colorHex = "#5C6BC0", isDefault = true, sortOrder = 7),
        CategoryEntity(name = "Other",            iconName = "more_horiz",         colorHex = "#BDBDBD", isDefault = true, sortOrder = 8),
        CategoryEntity(name = "Salary",           iconName = "work",               colorHex = "#66BB6A", isDefault = true, sortOrder = 9),
        CategoryEntity(name = "Freelance",        iconName = "laptop",             colorHex = "#42A5F5", isDefault = true, sortOrder = 10),
        CategoryEntity(name = "Investment",       iconName = "trending_up",        colorHex = "#FFCA28", isDefault = true, sortOrder = 11),
        CategoryEntity(name = "Other Income",     iconName = "attach_money",       colorHex = "#26C6DA", isDefault = true, sortOrder = 12)
    )
}
