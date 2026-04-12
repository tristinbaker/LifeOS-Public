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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lifeos_db"
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
