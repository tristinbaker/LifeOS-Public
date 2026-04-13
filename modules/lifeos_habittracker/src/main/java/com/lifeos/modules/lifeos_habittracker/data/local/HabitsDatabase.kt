package com.lifeos.modules.lifeos_habittracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromHabitFrequency(value: HabitFrequency): String = value.name

    @TypeConverter
    fun toHabitFrequency(value: String): HabitFrequency = HabitFrequency.valueOf(value)
}

@Database(
    entities = [HabitEntity::class, HabitCheckIn::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HabitsDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
