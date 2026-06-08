package com.lifeos.modules.lifeos_financetracker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType { INCOME, EXPENSE, TRANSFER }
enum class AccountType { CHECKING, SAVINGS, CREDIT_CARD, INVESTMENT, REAL_ESTATE, MORTGAGE }
enum class RecurringFrequency { DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, SEMI_ANNUAL, YEARLY }

fun AccountType.isAsset() = this in listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.INVESTMENT, AccountType.REAL_ESTATE)
fun AccountType.isTransactionDriven() = this in listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.CREDIT_CARD)
fun AccountType.isSnapshot() = this in listOf(AccountType.INVESTMENT, AccountType.REAL_ESTATE)

fun AccountType.displayName() = when (this) {
    AccountType.CHECKING -> "Checking"
    AccountType.SAVINGS -> "Savings"
    AccountType.CREDIT_CARD -> "Credit Card"
    AccountType.INVESTMENT -> "Investment"
    AccountType.REAL_ESTATE -> "Real Estate / Home"
    AccountType.MORTGAGE -> "Mortgage"
}

@Entity(tableName = "lifeos_financetracker_transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val toAccountId: Long? = null,
    val date: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lifeos_financetracker_categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val sortOrder: Int = 999
)

@Entity(
    tableName = "lifeos_financetracker_budgets",
    indices = [Index(value = ["categoryId"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val monthlyLimitCents: Long
)

@Entity(tableName = "lifeos_financetracker_accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val colorHex: String = "#78909C",
    val startingBalance: Double = 0.0,
    val sortOrder: Int = 999,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lifeos_financetracker_account_snapshots")
data class AccountSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val balance: Double,
    val date: String
)

@Entity(
    tableName = "lifeos_financetracker_mortgage_details",
    indices = [Index(value = ["accountId"], unique = true)]
)
data class MortgageDetailsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val currentBalance: Double,
    val aprPercent: Double,
    val remainingMonths: Int,
    val monthlyPayment: Double,
    val asOfDate: String
)

@Entity(tableName = "lifeos_financetracker_recurring")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val toAccountId: Long? = null,
    val frequency: RecurringFrequency,
    val dayOfMonth: Int? = null,
    val dayOfWeek: Int? = null,
    val nextPostDate: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lifeos_financetracker_sinking_funds")
data class SinkingFundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmountCents: Long,
    val initialBalanceCents: Long = 0,
    val targetDate: String,
    val colorHex: String = "#66BB6A",
    val accountId: Long? = null,
    val categoryId: Long? = null,
    val contributionDayOfMonth: Int? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lifeos_financetracker_sinking_contributions")
data class SinkingFundContributionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fundId: Long,
    val amountCents: Long,
    val date: String  // ISO "YYYY-MM-DD"
)

@Entity(
    tableName = "lifeos_financetracker_networth_history",
    indices = [Index(value = ["date"], unique = true)]
)
data class NetWorthSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double
)

@Entity(tableName = "lifeos_financetracker_personal_card_payments")
data class PersonalCardPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val note: String = "",
    val date: String,
    val createdAt: Long = System.currentTimeMillis()
)
