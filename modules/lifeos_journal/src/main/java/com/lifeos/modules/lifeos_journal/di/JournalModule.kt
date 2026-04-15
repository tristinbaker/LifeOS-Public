package com.lifeos.modules.lifeos_journal.di

import android.content.Context
import androidx.room.Room
import com.lifeos.modules.lifeos_journal.data.local.JournalDatabase
import com.lifeos.modules.lifeos_journal.data.local.JournalEntryDao
import com.lifeos.modules.lifeos_journal.data.local.JournalImageDao
import com.lifeos.modules.lifeos_journal.data.local.JournalSettingsDao
import com.lifeos.modules.lifeos_journal.data.local.JOURNAL_MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object JournalModule {
    @Provides
    @Singleton
    fun provideJournalDatabase(@ApplicationContext context: Context): JournalDatabase {
        return Room.databaseBuilder(
            context,
            JournalDatabase::class.java,
            "lifeos_journal.db"
        )
            .addMigrations(JOURNAL_MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideJournalEntryDao(database: JournalDatabase): JournalEntryDao {
        return database.journalEntryDao()
    }

    @Provides
    @Singleton
    fun provideJournalSettingsDao(database: JournalDatabase): JournalSettingsDao {
        return database.journalSettingsDao()
    }

    @Provides
    @Singleton
    fun provideJournalImageDao(database: JournalDatabase): JournalImageDao {
        return database.journalImageDao()
    }
}
