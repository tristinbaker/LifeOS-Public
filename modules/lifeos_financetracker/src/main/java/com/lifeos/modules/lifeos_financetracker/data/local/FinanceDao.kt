package com.lifeos.modules.lifeos_financetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM lifeos_financetracker_transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC, createdAt DESC")
    fun getTransactionsForMonth(start: String, end: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM lifeos_financetracker_transactions ORDER BY date DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM lifeos_financetracker_transactions WHERE accountId = :accountId OR toAccountId = :accountId ORDER BY date DESC, createdAt DESC")
    fun getTransactionsForAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM lifeos_financetracker_transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM lifeos_financetracker_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM lifeos_financetracker_categories ORDER BY sortOrder ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM lifeos_financetracker_categories ORDER BY sortOrder ASC, name ASC")
    suspend fun getAllCategoriesOnce(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM lifeos_financetracker_categories")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM lifeos_financetracker_budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)

    @Query("DELETE FROM lifeos_financetracker_budgets WHERE categoryId = :categoryId")
    suspend fun deleteForCategory(categoryId: Long)
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM lifeos_financetracker_accounts WHERE isActive = 1 ORDER BY sortOrder ASC, name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM lifeos_financetracker_accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Query("UPDATE lifeos_financetracker_accounts SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)
}

@Dao
interface AccountSnapshotDao {
    @Query("SELECT * FROM lifeos_financetracker_account_snapshots")
    fun getAllSnapshots(): Flow<List<AccountSnapshotEntity>>

    @Query("SELECT * FROM lifeos_financetracker_account_snapshots WHERE accountId = :accountId ORDER BY date DESC")
    fun getSnapshotsForAccount(accountId: Long): Flow<List<AccountSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: AccountSnapshotEntity): Long

    @Query("DELETE FROM lifeos_financetracker_account_snapshots WHERE accountId = :accountId")
    suspend fun deleteAllForAccount(accountId: Long)
}

@Dao
interface MortgageDetailsDao {
    @Query("SELECT * FROM lifeos_financetracker_mortgage_details")
    fun getAllMortgageDetails(): Flow<List<MortgageDetailsEntity>>

    @Query("SELECT * FROM lifeos_financetracker_mortgage_details WHERE accountId = :accountId")
    suspend fun getForAccount(accountId: Long): MortgageDetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(details: MortgageDetailsEntity)

    @Query("DELETE FROM lifeos_financetracker_mortgage_details WHERE accountId = :accountId")
    suspend fun deleteForAccount(accountId: Long)
}

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM lifeos_financetracker_recurring WHERE isActive = 1 ORDER BY nextPostDate ASC")
    fun getActiveRecurring(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM lifeos_financetracker_recurring WHERE nextPostDate <= :date AND isActive = 1")
    suspend fun getDueRecurring(date: String): List<RecurringTransactionEntity>

    @Query("SELECT * FROM lifeos_financetracker_recurring WHERE id = :id")
    suspend fun getById(id: Long): RecurringTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurring: RecurringTransactionEntity): Long

    @Update
    suspend fun update(recurring: RecurringTransactionEntity)

    @Query("DELETE FROM lifeos_financetracker_recurring WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE lifeos_financetracker_recurring SET isActive = :isActive WHERE id = :id")
    suspend fun setActive(id: Long, isActive: Boolean)
}

@Dao
interface SinkingFundDao {
    @Query("SELECT * FROM lifeos_financetracker_sinking_funds WHERE isActive = 1 ORDER BY targetDate ASC")
    fun getAllActive(): Flow<List<SinkingFundEntity>>

    @Query("SELECT * FROM lifeos_financetracker_sinking_funds WHERE isActive = 1 AND contributionDayOfMonth = :day")
    suspend fun getDueToday(day: Int): List<SinkingFundEntity>

    @Query("SELECT * FROM lifeos_financetracker_sinking_funds WHERE id = :id")
    suspend fun getById(id: Long): SinkingFundEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SinkingFundEntity): Long

    @Update
    suspend fun update(entity: SinkingFundEntity)

    @Query("DELETE FROM lifeos_financetracker_sinking_funds WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface SinkingFundContributionDao {
    @Query("SELECT * FROM lifeos_financetracker_sinking_contributions WHERE fundId = :fundId ORDER BY date DESC")
    fun getForFund(fundId: Long): Flow<List<SinkingFundContributionEntity>>

    @Query("SELECT * FROM lifeos_financetracker_sinking_contributions ORDER BY date DESC")
    fun getAll(): Flow<List<SinkingFundContributionEntity>>

    @Query("SELECT * FROM lifeos_financetracker_sinking_contributions WHERE fundId = :fundId AND date LIKE :monthPrefix || '%'")
    suspend fun getForFundInMonth(fundId: Long, monthPrefix: String): List<SinkingFundContributionEntity>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM lifeos_financetracker_sinking_contributions WHERE fundId = :fundId")
    suspend fun getTotalContributedCents(fundId: Long): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SinkingFundContributionEntity): Long

    @Query("DELETE FROM lifeos_financetracker_sinking_contributions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM lifeos_financetracker_sinking_contributions WHERE fundId = :fundId")
    suspend fun deleteAllForFund(fundId: Long)
}

@Dao
interface NetWorthHistoryDao {
    @Query("SELECT * FROM lifeos_financetracker_networth_history ORDER BY date ASC LIMIT :limit")
    fun getRecentHistory(limit: Int = 90): Flow<List<NetWorthSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(snapshot: NetWorthSnapshotEntity)
}

@Dao
interface PersonalCardPaymentDao {
    @Query("SELECT * FROM lifeos_financetracker_personal_card_payments ORDER BY date DESC, createdAt DESC")
    fun getAll(): Flow<List<PersonalCardPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PersonalCardPaymentEntity): Long

    @Query("DELETE FROM lifeos_financetracker_personal_card_payments WHERE id = :id")
    suspend fun deleteById(id: Long)
}
