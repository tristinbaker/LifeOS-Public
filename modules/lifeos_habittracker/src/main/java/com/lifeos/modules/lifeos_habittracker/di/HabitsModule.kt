package com.lifeos.modules.lifeos_habittracker.di

import android.content.Context
import androidx.room.Room
import com.lifeos.modules.lifeos_habittracker.data.local.HabitDao
import com.lifeos.modules.lifeos_habittracker.data.local.HABITS_MIGRATION_1_2
import com.lifeos.modules.lifeos_habittracker.data.local.HabitsDatabase
import com.lifeos.modules.lifeos_habittracker.notification.HabitReminderScheduler
import com.lifeos.core.ReportDataProvider
import com.lifeos.modules.lifeos_habittracker.data.repository.HabitsRepository
import com.lifeos.modules.lifeos_habittracker.report.HabitReportProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HabitsModule {
    @Provides
    @Singleton
    fun provideHabitsDatabase(@ApplicationContext context: Context): HabitsDatabase {
        return Room.databaseBuilder(
            context,
            HabitsDatabase::class.java,
            "lifeos_habittracker.db"
        ).addMigrations(HABITS_MIGRATION_1_2).build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: HabitsDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    @Singleton
    fun provideHabitReminderScheduler(@ApplicationContext context: Context): HabitReminderScheduler {
        return HabitReminderScheduler(context)
    }

    @Provides
    @IntoSet
    @Singleton
    fun provideHabitReportProvider(repository: HabitsRepository, dao: HabitDao): ReportDataProvider =
        HabitReportProvider(repository, dao)
}
