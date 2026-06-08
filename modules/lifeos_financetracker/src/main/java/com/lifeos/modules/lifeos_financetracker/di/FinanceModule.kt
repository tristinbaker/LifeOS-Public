package com.lifeos.modules.lifeos_financetracker.di

import android.content.Context
import androidx.room.Room
import com.lifeos.modules.lifeos_financetracker.data.local.*
import com.lifeos.modules.lifeos_financetracker.notification.RecurringTransactionScheduler
import com.lifeos.core.InsightProvider
import com.lifeos.core.ReportDataProvider
import com.lifeos.core.UserPrefs
import com.lifeos.modules.lifeos_financetracker.data.repository.FinanceRepository
import com.lifeos.modules.lifeos_financetracker.report.FinanceReportProvider
import com.lifeos.modules.lifeos_financetracker.report.RetirementOutlookInsightProvider
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
    fun provideSinkingFundDao(db: FinanceDatabase): SinkingFundDao = db.sinkingFundDao()

    @Provides @Singleton
    fun provideSinkingFundContributionDao(db: FinanceDatabase): SinkingFundContributionDao = db.sinkingFundContributionDao()

    @Provides @Singleton
    fun providePersonalCardPaymentDao(db: FinanceDatabase): PersonalCardPaymentDao = db.personalCardPaymentDao()

    @Provides @Singleton
    fun provideRecurringTransactionScheduler(@ApplicationContext context: Context): RecurringTransactionScheduler =
        RecurringTransactionScheduler(context)

    @Provides
    @IntoSet
    @Singleton
    fun provideFinanceReportProvider(repository: FinanceRepository): ReportDataProvider =
        FinanceReportProvider(repository)

    @Provides
    @IntoSet
    @Singleton
    fun provideRetirementOutlookInsightProvider(
        netWorthDao: NetWorthHistoryDao,
        sinkingFundDao: SinkingFundDao,
        accountDao: AccountDao,
        accountSnapshotDao: AccountSnapshotDao,
        userPrefs: UserPrefs
    ): InsightProvider = RetirementOutlookInsightProvider(netWorthDao, sinkingFundDao, accountDao, accountSnapshotDao, userPrefs)
}
