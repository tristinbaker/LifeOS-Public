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
import javax.inject.Inject

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
    val recurringTransactions: List<RecurringTransactionEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<FinanceUiState> = combine(
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
        ) { recurring, history -> Pair(recurring, history) }
    ) { accountsWithBalances, (monthlyTxns, cats, budgets), (recurring, history) ->
        val assets = accountsWithBalances.filter { it.account.type.isAsset() }.sumOf { it.balance }
        val liabilities = accountsWithBalances.filter { !it.account.type.isAsset() }.sumOf { it.balance }
        val budgetMap = budgets.associateBy { it.categoryId }
        val expensesByCat = monthlyTxns.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }.mapValues { (_, list) -> list.sumOf { it.amount } }
        val categorySpending = cats.filter { expensesByCat.containsKey(it.id) || budgetMap.containsKey(it.id) }
            .map { cat ->
                CategorySpending(
                    category = cat,
                    totalSpent = expensesByCat[cat.id] ?: 0.0,
                    budgetLimitCents = budgetMap[cat.id]?.monthlyLimitCents
                )
            }.sortedByDescending { it.totalSpent }

        FinanceUiState(
            netWorth = assets - liabilities,
            totalAssets = assets,
            totalLiabilities = liabilities,
            netWorthHistory = history,
            accounts = accountsWithBalances,
            selectedMonth = _selectedMonth.value,
            totalIncome = monthlyTxns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
            totalExpenses = monthlyTxns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
            monthlyTransactions = monthlyTxns,
            categorySpending = categorySpending,
            categories = cats,
            budgets = budgets,
            recurringTransactions = recurring
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

    fun updateSnapshot(accountId: Long, balance: Double, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertSnapshot(
                AccountSnapshotEntity(
                    accountId = accountId,
                    balance = balance,
                    date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
            )
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
            if (id != null && id > 0) repository.updateTransaction(tx)
            else repository.insertTransaction(tx)
            onComplete()
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
}
