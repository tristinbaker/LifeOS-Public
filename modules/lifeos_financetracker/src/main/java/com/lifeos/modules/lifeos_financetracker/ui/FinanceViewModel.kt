package com.lifeos.modules.lifeos_financetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_financetracker.data.local.*
import com.lifeos.modules.lifeos_financetracker.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.ceil

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

data class SinkingFundProgress(
    val fund: SinkingFundEntity,
    val currentSavedCents: Long,
    val monthsRemaining: Int,
    val monthlyContributionCents: Long,
    val progressFraction: Float,
    val contributions: List<SinkingFundContributionEntity>
)

data class MonthlyTrend(
    val month: YearMonth,
    val income: Double,
    val expenses: Double
) {
    val net: Double get() = income - expenses
}

data class FinanceUiState(
    val netWorth: Double = 0.0,
    val totalAssets: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val netWorthHistory: List<NetWorthSnapshotEntity> = emptyList(),
    val accounts: List<AccountWithBalance> = emptyList(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val monthlyTransactions: List<TransactionEntity> = emptyList(),
    val categorySpending: List<CategorySpending> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val recurringTransactions: List<RecurringTransactionEntity> = emptyList(),
    val monthlyTrends: List<MonthlyTrend> = emptyList(),
    val sinkingFunds: List<SinkingFundProgress> = emptyList(),
    val expectedMonthlyIncome: Double = 0.0,
    val personalCardBalance: Double = 0.0,
    val personalCardTransactions: List<TransactionEntity> = emptyList(),
    val personalCardPayments: List<PersonalCardPaymentEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<FinanceUiState> = combine(
        combine(
            repository.getAllAccountsWithBalance(),
            _selectedMonth.flatMapLatest { month ->
                combine(
                    repository.getTransactionsForMonth(month),
                    repository.getAllCategories(),
                    repository.getAllBudgets()
                ) { txns, cats, budgets -> Triple(txns, cats, budgets) }
            },
            combine(
                repository.getActiveRecurring(),
                repository.getNetWorthHistory()
            ) { recurring, history -> Pair(recurring, history) },
            combine(
                repository.getAllTransactions(),
                repository.getAllSinkingFunds(),
                repository.getAllContributions()
            ) { allTxns, funds, contributions -> Triple(allTxns, funds, contributions) }
        ) { accountsWithBalances, monthTriple, recurringHistoryPair, sinkingTriple ->
            Quad(accountsWithBalances, monthTriple, recurringHistoryPair, sinkingTriple)
        },
        repository.getPersonalCardPayments()
    ) { (accountsWithBalances, monthTriple, recurringHistoryPair, sinkingTriple), cardPayments ->
        val (monthlyTxns, cats, budgets) = monthTriple
        val (recurring, history) = recurringHistoryPair
        val (allTxns, funds, contributions) = sinkingTriple
        val assets = accountsWithBalances.filter { it.account.type.isAsset() }.sumOf { it.balance }
        val liabilities = accountsWithBalances.filter { !it.account.type.isAsset() }.sumOf { it.balance }
        val budgetMap = budgets.associateBy { it.categoryId }
        val expensesByCat = monthlyTxns
            .filter { it.type == TransactionType.EXPENSE || it.type == TransactionType.TRANSFER }
            .groupBy { it.categoryId }.mapValues { (_, list) -> list.sumOf { it.amount } }
        val categorySpending = cats.filter { expensesByCat.containsKey(it.id) || budgetMap.containsKey(it.id) }
            .map { cat ->
                CategorySpending(
                    category = cat,
                    totalSpent = expensesByCat[cat.id] ?: 0.0,
                    budgetLimitCents = budgetMap[cat.id]?.monthlyLimitCents
                )
            }.sortedByDescending { it.totalSpent }
        val contributionsByFund = contributions.groupBy { it.fundId }
        val sinkingFundProgress = funds.map { fund ->
            val fundContributions = contributionsByFund[fund.id] ?: emptyList()
            val currentSaved = fund.initialBalanceCents + fundContributions.sumOf { it.amountCents }
            val targetYearMonth = try { YearMonth.from(LocalDate.parse(fund.targetDate)) } catch (e: Exception) { YearMonth.now() }
            val monthsRemaining = (ChronoUnit.MONTHS.between(YearMonth.now(), targetYearMonth).toInt() + 1).coerceAtLeast(1)
            val remaining = (fund.targetAmountCents - currentSaved).coerceAtLeast(0L)
            val monthly = if (remaining == 0L) 0L else ceil(remaining.toDouble() / monthsRemaining).toLong()
            SinkingFundProgress(
                fund = fund,
                currentSavedCents = currentSaved,
                monthsRemaining = monthsRemaining,
                monthlyContributionCents = monthly,
                progressFraction = if (fund.targetAmountCents > 0) (currentSaved.toFloat() / fund.targetAmountCents).coerceIn(0f, 1f) else 0f,
                contributions = fundContributions.sortedByDescending { it.date }
            )
        }

        val selectedMonth = _selectedMonth.value
        val actualIncome = monthlyTxns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expectedIncome = actualIncome + projectRemainingRecurringIncome(recurring, selectedMonth)
        val actualExpenses = monthlyTxns.filter { it.type == TransactionType.EXPENSE || it.type == TransactionType.TRANSFER }.sumOf { it.amount }
        val personalCat = cats.find { it.name == "Personal" }
        val personalTxns = if (personalCat != null)
            allTxns.filter { it.categoryId == personalCat.id && (it.type == TransactionType.EXPENSE || it.type == TransactionType.TRANSFER) }
        else emptyList()
        val cardCharges = personalTxns.sumOf { it.amount }
        val cardPaid = cardPayments.sumOf { it.amountCents / 100.0 }
        val cardBalance = cardCharges - cardPaid

        FinanceUiState(
            netWorth = assets - liabilities,
            totalAssets = assets,
            totalLiabilities = liabilities,
            netWorthHistory = history,
            accounts = accountsWithBalances,
            selectedMonth = selectedMonth,
            totalIncome = actualIncome,
            totalExpenses = actualExpenses,
            monthlyTransactions = monthlyTxns,
            categorySpending = categorySpending,
            categories = cats,
            budgets = budgets,
            recurringTransactions = recurring,
            monthlyTrends = buildMonthlyTrends(allTxns),
            sinkingFunds = sinkingFundProgress,
            expectedMonthlyIncome = expectedIncome,
            personalCardBalance = cardBalance,
            personalCardTransactions = personalTxns,
            personalCardPayments = cardPayments
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, FinanceUiState())

    init {
        // Record net worth snapshot whenever balances settle
        viewModelScope.launch {
            uiState.drop(1).collect { state ->
                if (state.accounts.isNotEmpty()) {
                    repository.recordNetWorthSnapshot(state.totalAssets, state.totalLiabilities)
                }
            }
        }
    }

    // ---- Month navigation ----

    fun navigateToPreviousMonth() { _selectedMonth.value = _selectedMonth.value.minusMonths(1) }
    fun navigateToNextMonth() { _selectedMonth.value = _selectedMonth.value.plusMonths(1) }
    fun navigateToCurrentMonth() { _selectedMonth.value = YearMonth.now() }

    // ---- Accounts ----

    fun saveAccount(
        id: Long?,
        name: String,
        type: AccountType,
        colorHex: String,
        startingBalance: Double,
        onComplete: (Long?) -> Unit
    ) {
        viewModelScope.launch {
            val account = AccountEntity(
                id = id ?: 0,
                name = name,
                type = type,
                colorHex = colorHex,
                startingBalance = startingBalance
            )
            val savedId = if (id != null && id > 0) {
                repository.updateAccount(account)
                id
            } else {
                repository.insertAccount(account)
            }
            onComplete(savedId)
        }
    }

    fun softDeleteAccount(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.softDeleteAccount(id)
            onComplete()
        }
    }

    fun updateSnapshot(awb: AccountWithBalance, target: Double, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (awb.account.type.isTransactionDriven()) {
                // Shift startingBalance so that (startingBalance + all transaction effects) == target
                val adjustment = target - awb.balance
                repository.updateAccount(awb.account.copy(startingBalance = awb.account.startingBalance + adjustment))
            } else {
                repository.insertSnapshot(
                    AccountSnapshotEntity(
                        accountId = awb.account.id,
                        balance = target,
                        date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    )
                )
            }
            onComplete()
        }
    }

    fun saveMortgageDetails(
        accountId: Long,
        currentBalance: Double,
        aprPercent: Double,
        remainingMonths: Int,
        monthlyPayment: Double,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.upsertMortgageDetails(
                MortgageDetailsEntity(
                    accountId = accountId,
                    currentBalance = currentBalance,
                    aprPercent = aprPercent,
                    remainingMonths = remainingMonths,
                    monthlyPayment = monthlyPayment,
                    asOfDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
            )
            onComplete()
        }
    }

    // ---- Transactions ----

    fun saveTransaction(
        id: Long?,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        accountId: Long,
        toAccountId: Long?,
        date: String,
        note: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val oldTx = if (id != null && id > 0) repository.getTransactionById(id) else null
            val tx = TransactionEntity(
                id = id ?: 0,
                amount = amount,
                type = type,
                categoryId = categoryId,
                accountId = accountId,
                toAccountId = toAccountId,
                date = date,
                note = note
            )
            if (id != null && id > 0) repository.updateTransaction(tx) else repository.insertTransaction(tx)
            syncMortgageAfterTransaction(oldTx, tx)
            onComplete()
        }
    }

    private suspend fun syncMortgageAfterTransaction(
        oldTx: TransactionEntity?,
        newTx: TransactionEntity
    ) {
        val oldMortgageId = oldTx?.takeIf { it.type == TransactionType.TRANSFER }?.toAccountId
        val newMortgageId = newTx.takeIf { it.type == TransactionType.TRANSFER }?.toAccountId

        if (oldMortgageId == null && newMortgageId == null) return

        // Reverse the old payment if the mortgage account changed
        if (oldMortgageId != null && oldMortgageId != newMortgageId) {
            repository.getMortgageDetails(oldMortgageId)?.let { details ->
                val restored = repository.computeMortgageBalance(details) + oldTx!!.amount
                repository.upsertMortgageDetails(details.copy(currentBalance = restored))
            }
        }

        // Apply payment to the new (or unchanged) mortgage account
        if (newMortgageId != null) {
            repository.getMortgageDetails(newMortgageId)?.let { details ->
                val base = repository.computeMortgageBalance(details)
                val reversal = if (oldMortgageId == newMortgageId) oldTx!!.amount else 0.0
                repository.upsertMortgageDetails(details.copy(
                    currentBalance = maxOf(0.0, base - newTx.amount + reversal),
                    asOfDate = newTx.date
                ))
            }
        }
    }

    fun deleteTransaction(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            onComplete()
        }
    }

    // ---- Categories ----

    fun saveCategory(id: Long?, name: String, iconName: String, colorHex: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val cat = CategoryEntity(id = id ?: 0, name = name, iconName = iconName, colorHex = colorHex)
            if (id != null && id > 0) repository.updateCategory(cat)
            else repository.insertCategory(cat)
            onComplete()
        }
    }

    fun deleteCategory(category: CategoryEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            onComplete()
        }
    }

    // ---- Budgets ----

    fun setBudget(categoryId: Long, amountDollars: Double, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.upsertBudget(BudgetEntity(categoryId = categoryId, monthlyLimitCents = (amountDollars * 100).toLong()))
            onComplete()
        }
    }

    fun removeBudget(categoryId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteBudgetForCategory(categoryId)
            onComplete()
        }
    }

    // ---- Recurring ----

    fun saveRecurring(
        id: Long?,
        label: String,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        accountId: Long,
        toAccountId: Long?,
        frequency: RecurringFrequency,
        dayOfMonth: Int?,
        dayOfWeek: Int?,
        nextPostDate: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val r = RecurringTransactionEntity(
                id = id ?: 0,
                label = label,
                amount = amount,
                type = type,
                categoryId = categoryId,
                accountId = accountId,
                toAccountId = toAccountId,
                frequency = frequency,
                dayOfMonth = dayOfMonth,
                dayOfWeek = dayOfWeek,
                nextPostDate = nextPostDate
            )
            if (id != null && id > 0) repository.updateRecurring(r)
            else repository.insertRecurring(r)
            onComplete()
        }
    }

    fun deleteRecurring(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteRecurring(id)
            onComplete()
        }
    }

    fun toggleRecurring(id: Long, isActive: Boolean) {
        viewModelScope.launch {
            repository.setRecurringActive(id, isActive)
        }
    }

    // ---- Sinking funds ----

    fun saveSinkingFund(
        id: Long?,
        name: String,
        targetDollars: Double,
        initialDollars: Double,
        targetDate: String,
        colorHex: String,
        accountId: Long?,
        categoryId: Long?,
        contributionDayOfMonth: Int?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val entity = SinkingFundEntity(
                id = id ?: 0,
                name = name,
                targetAmountCents = (targetDollars * 100).toLong(),
                initialBalanceCents = (initialDollars * 100).toLong(),
                targetDate = targetDate,
                colorHex = colorHex,
                accountId = accountId,
                categoryId = categoryId,
                contributionDayOfMonth = contributionDayOfMonth
            )
            if (id != null && id > 0) repository.updateSinkingFund(entity)
            else repository.insertSinkingFund(entity)
            onComplete()
        }
    }

    fun deleteSinkingFund(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteSinkingFund(id)
            onComplete()
        }
    }

    fun logSinkingContribution(fundId: Long, amountDollars: Double, date: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertContribution(
                SinkingFundContributionEntity(
                    fundId = fundId,
                    amountCents = (amountDollars * 100).toLong(),
                    date = date
                )
            )
            val fund = repository.getSinkingFundById(fundId)
            if (fund?.accountId != null) {
                repository.insertTransaction(
                    TransactionEntity(
                        amount = amountDollars,
                        type = TransactionType.EXPENSE,
                        categoryId = fund.categoryId ?: 0L,
                        accountId = fund.accountId,
                        date = date,
                        note = fund.name
                    )
                )
            }
            onComplete()
        }
    }

    fun deleteContribution(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteContribution(id)
            onComplete()
        }
    }

    // ---- Personal card payments ----

    fun addPersonalCardPayment(amountCents: Long, note: String, date: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertPersonalCardPayment(
                PersonalCardPaymentEntity(amountCents = amountCents, note = note, date = date)
            )
            onComplete()
        }
    }

    fun deletePersonalCardPayment(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deletePersonalCardPayment(id)
            onComplete()
        }
    }

    // Projects recurring INCOME that hasn't fired yet in the selected month.
    // For past months returns 0 (actuals are sufficient). For the current month,
    // returns the sum of expected-but-not-yet-posted recurring income so the budget
    // summary shows the full expected paycheck instead of $0 until payday.
    private fun projectRemainingRecurringIncome(
        recurring: List<RecurringTransactionEntity>,
        month: YearMonth
    ): Double {
        if (month.isBefore(YearMonth.now())) return 0.0
        val today = LocalDate.now()
        val monthStart = month.atDay(1)
        val monthEnd = month.atEndOfMonth()
        return recurring
            .filter { it.isActive && it.type == TransactionType.INCOME }
            .sumOf { r ->
                var date = try { LocalDate.parse(r.nextPostDate) } catch (e: Exception) { return@sumOf 0.0 }
                var total = 0.0
                while (!date.isAfter(monthEnd)) {
                    if (!date.isBefore(monthStart) && date.isAfter(today)) {
                        total += r.amount
                    }
                    val next = advanceRecurringDate(r, date)
                    if (!next.isAfter(date)) break  // guard against infinite loop
                    date = next
                }
                total
            }
    }

    private fun advanceRecurringDate(r: RecurringTransactionEntity, from: LocalDate): LocalDate = when (r.frequency) {
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

    private fun buildMonthlyTrends(txns: List<TransactionEntity>): List<MonthlyTrend> {
        val now = YearMonth.now()
        return (0..11).map { offset ->
            val month = now.minusMonths(offset.toLong())
            val monthTxns = txns.filter {
                try { YearMonth.from(LocalDate.parse(it.date)) == month } catch (e: Exception) { false }
            }
            MonthlyTrend(
                month = month,
                income = monthTxns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                expenses = monthTxns.filter { it.type == TransactionType.EXPENSE || it.type == TransactionType.TRANSFER }.sumOf { it.amount }
            )
        }.reversed()
    }
}
