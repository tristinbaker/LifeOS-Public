package com.lifeos.modules.lifeos_financetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class FinanceConverters {
    @TypeConverter fun fromTransactionType(v: TransactionType): String = v.name
    @TypeConverter fun toTransactionType(v: String): TransactionType = TransactionType.valueOf(v)
    @TypeConverter fun fromAccountType(v: AccountType): String = v.name
    @TypeConverter fun toAccountType(v: String): AccountType = AccountType.valueOf(v)
    @TypeConverter fun fromRecurringFrequency(v: RecurringFrequency): String = v.name
    @TypeConverter fun toRecurringFrequency(v: String): RecurringFrequency = RecurringFrequency.valueOf(v)
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE lifeos_financetracker_transactions ADD COLUMN accountId INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE lifeos_financetracker_transactions ADD COLUMN toAccountId INTEGER")
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS lifeos_financetracker_accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                colorHex TEXT NOT NULL DEFAULT '#78909C',
                startingBalance REAL NOT NULL DEFAULT 0.0,
                sortOrder INTEGER NOT NULL DEFAULT 999,
                isActive INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL DEFAULT 0)"""
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS lifeos_financetracker_account_snapshots (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                accountId INTEGER NOT NULL,
                balance REAL NOT NULL,
                date TEXT NOT NULL)"""
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS lifeos_financetracker_mortgage_details (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                accountId INTEGER NOT NULL,
                currentBalance REAL NOT NULL,
                aprPercent REAL NOT NULL,
                remainingMonths INTEGER NOT NULL,
                monthlyPayment REAL NOT NULL,
                asOfDate TEXT NOT NULL)"""
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_mortgage_accountId ON lifeos_financetracker_mortgage_details (accountId)")
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS lifeos_financetracker_recurring (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                label TEXT NOT NULL,
                amount REAL NOT NULL,
                type TEXT NOT NULL,
                categoryId INTEGER NOT NULL,
                accountId INTEGER NOT NULL,
                toAccountId INTEGER,
                frequency TEXT NOT NULL,
                dayOfMonth INTEGER,
                dayOfWeek INTEGER,
                nextPostDate TEXT NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL DEFAULT 0)"""
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS lifeos_financetracker_networth_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                totalAssets REAL NOT NULL,
                totalLiabilities REAL NOT NULL,
                netWorth REAL NOT NULL)"""
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_networth_date ON lifeos_financetracker_networth_history (date)")
    }
}

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        AccountEntity::class,
        AccountSnapshotEntity::class,
        MortgageDetailsEntity::class,
        RecurringTransactionEntity::class,
        NetWorthSnapshotEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(FinanceConverters::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun accountDao(): AccountDao
    abstract fun accountSnapshotDao(): AccountSnapshotDao
    abstract fun mortgageDetailsDao(): MortgageDetailsDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun netWorthHistoryDao(): NetWorthHistoryDao
}
