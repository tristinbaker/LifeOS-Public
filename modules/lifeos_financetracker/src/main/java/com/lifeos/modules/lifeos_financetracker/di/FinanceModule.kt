package com.lifeos.modules.lifeos_financetracker.di

import android.content.Context
import androidx.room.Room
import com.lifeos.modules.lifeos_financetracker.data.local.*
import com.lifeos.modules.lifeos_financetracker.notification.RecurringTransactionScheduler
import com.lifeos.core.ReportDataProvider
import com.lifeos.modules.lifeos_financetracker.data.repository.FinanceRepository
import com.lifeos.modules.lifeos_financetracker.report.FinanceReportProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FinanceModule {

    @Provides
    @Singleton
    fun provideFinanceDatabase(@ApplicationContext context: Context): FinanceDatabase =
        Room.databaseBuilder(context, FinanceDatabase::class.java, "lifeos_financetracker.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides @Singleton
    fun provideTransactionDao(db: FinanceDatabase): TransactionDao = db.transactionDao()

    @Provides @Singleton
    fun provideCategoryDao(db: FinanceDatabase): CategoryDao = db.categoryDao()

    @Provides @Singleton
    fun provideBudgetDao(db: FinanceDatabase): BudgetDao = db.budgetDao()

    @Provides @Singleton
    fun provideAccountDao(db: FinanceDatabase): AccountDao = db.accountDao()

    @Provides @Singleton
    fun provideAccountSnapshotDao(db: FinanceDatabase): AccountSnapshotDao = db.accountSnapshotDao()

    @Provides @Singleton
    fun provideMortgageDetailsDao(db: FinanceDatabase): MortgageDetailsDao = db.mortgageDetailsDao()

    @Provides @Singleton
    fun provideRecurringTransactionDao(db: FinanceDatabase): RecurringTransactionDao = db.recurringTransactionDao()

    @Provides @Singleton
    fun provideNetWorthHistoryDao(db: FinanceDatabase): NetWorthHistoryDao = db.netWorthHistoryDao()

    @Provides @Singleton
    fun provideRecurringTransactionScheduler(@ApplicationContext context: Context): RecurringTransactionScheduler =
        RecurringTransactionScheduler(context)

    @Provides
    @IntoSet
    @Singleton
    fun provideFinanceReportProvider(repository: FinanceRepository): ReportDataProvider =
        FinanceReportProvider(repository)
}
