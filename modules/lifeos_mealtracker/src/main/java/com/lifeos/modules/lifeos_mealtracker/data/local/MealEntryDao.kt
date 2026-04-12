package com.lifeos.modules.lifeos_mealtracker.data.local

import androidx.room.*
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MealEntryDao {
    @Query("SELECT * FROM lifeos_mealtracker_meal_entries WHERE date = :date ORDER BY createdAt ASC")
    fun getMealsByDate(date: LocalDate): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM lifeos_mealtracker_meal_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, createdAt ASC")
    fun getMealsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM lifeos_mealtracker_meal_entries ORDER BY date DESC, createdAt DESC")
    fun getAllMeals(): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM lifeos_mealtracker_meal_entries WHERE id = :id")
    suspend fun getMealById(id: Long): MealEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntryEntity): Long

    @Update
    suspend fun updateMeal(meal: MealEntryEntity)

    @Delete
    suspend fun deleteMeal(meal: MealEntryEntity)

    @Query("DELETE FROM lifeos_mealtracker_meal_entries WHERE date = :date")
    suspend fun deleteMealsByDate(date: LocalDate)
}
