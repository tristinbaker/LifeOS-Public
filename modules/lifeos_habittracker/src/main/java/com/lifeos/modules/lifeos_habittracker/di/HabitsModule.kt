package com.lifeos.modules.lifeos_habittracker.di

import android.content.Context
import androidx.room.Room
import com.lifeos.modules.lifeos_habittracker.data.local.HabitDao
import com.lifeos.modules.lifeos_habittracker.data.local.HabitsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        ).build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: HabitsDatabase): HabitDao {
        return database.habitDao()
    }
}
