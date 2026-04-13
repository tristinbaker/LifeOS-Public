package com.lifeos.modules.lifeos_habittracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Converters {
    @TypeConverter
    fun fromHabitFrequency(value: HabitFrequency): String = value.name

    @TypeConverter
    fun toHabitFrequency(value: String): HabitFrequency = HabitFrequency.valueOf(value)
}

val HABITS_MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE lifeos_habittracker_habits ADD COLUMN reminderDays TEXT NOT NULL DEFAULT ''")
    }
}

@Database(
    entities = [HabitEntity::class, HabitCheckIn::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HabitsDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
