package com.lifeos.modules.lifeos_sleeptracker.di

import android.content.Context
import androidx.room.Room
import com.lifeos.modules.lifeos_sleeptracker.data.local.SleepDatabase
import com.lifeos.modules.lifeos_sleeptracker.data.local.SleepLogDao
import com.lifeos.modules.lifeos_sleeptracker.data.local.SleepSettingsDao
import com.lifeos.core.InsightProvider
import com.lifeos.core.ReportDataProvider
import com.lifeos.modules.lifeos_sleeptracker.data.repository.SleepRepository
import com.lifeos.modules.lifeos_sleeptracker.report.SleepMedicationInsightProvider
import com.lifeos.modules.lifeos_sleeptracker.report.SleepReportProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SleepModule {
    @Provides
    @Singleton
    fun provideSleepDatabase(@ApplicationContext context: Context): SleepDatabase {
        return Room.databaseBuilder(
            context,
            SleepDatabase::class.java,
            "lifeos_sleeptracker.db"
        ).addMigrations(SleepDatabase.MIGRATION_1_2).build()
    }

    @Provides
    @Singleton
    fun provideSleepLogDao(database: SleepDatabase): SleepLogDao {
        return database.sleepLogDao()
    }

    @Provides
    @Singleton
    fun provideSleepSettingsDao(database: SleepDatabase): SleepSettingsDao {
        return database.sleepSettingsDao()
    }

    @Provides
    @IntoSet
    @Singleton
    fun provideSleepReportProvider(repository: SleepRepository): ReportDataProvider =
        SleepReportProvider(repository)

    @Provides
    @IntoSet
    @Singleton
    fun provideSleepMedicationInsightProvider(repository: SleepRepository): InsightProvider =
        SleepMedicationInsightProvider(repository)
}
