package com.lifeos.modules.lifeos_mealtracker.data.local

import androidx.room.TypeConverter
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealType
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? = dateString?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromMealType(mealType: MealType): String = mealType.name

    @TypeConverter
    fun toMealType(value: String): MealType = MealType.valueOf(value)
}
