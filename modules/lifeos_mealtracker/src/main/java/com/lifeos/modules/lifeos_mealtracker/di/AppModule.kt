package com.lifeos.modules.lifeos_mealtracker.di

import android.content.Context
import androidx.room.Room
import com.lifeos.modules.lifeos_mealtracker.data.local.AppDatabase
import com.lifeos.modules.lifeos_mealtracker.data.local.MealEntryDao
import com.lifeos.modules.lifeos_mealtracker.data.local.SavedMealDao
import com.lifeos.modules.lifeos_mealtracker.data.local.WeightEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        // Migrate from the old shared "lifeos_db" filename to a module-specific one
        val oldDb = context.getDatabasePath("lifeos_db")
        val newDb = context.getDatabasePath("lifeos_mealtracker.db")
        if (oldDb.exists() && !newDb.exists()) {
            oldDb.renameTo(newDb)
            File("${oldDb.path}-shm").takeIf { it.exists() }?.renameTo(File("${newDb.path}-shm"))
            File("${oldDb.path}-wal").takeIf { it.exists() }?.renameTo(File("${newDb.path}-wal"))
        }
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lifeos_mealtracker.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMealEntryDao(database: AppDatabase): MealEntryDao {
        return database.mealEntryDao()
    }

    @Provides
    @Singleton
    fun provideSavedMealDao(database: AppDatabase): SavedMealDao {
        return database.savedMealDao()
    }

    @Provides
    @Singleton
    fun provideWeightEntryDao(database: AppDatabase): WeightEntryDao {
        return database.weightEntryDao()
    }
}
