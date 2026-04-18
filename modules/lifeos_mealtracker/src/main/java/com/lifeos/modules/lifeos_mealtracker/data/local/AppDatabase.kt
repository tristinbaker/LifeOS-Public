package com.lifeos.modules.lifeos_mealtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MealEntryEntity::class, SavedMealEntity::class, WeightEntryEntity::class, StoredItemEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun savedMealDao(): SavedMealDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun storedItemDao(): StoredItemDao
}
