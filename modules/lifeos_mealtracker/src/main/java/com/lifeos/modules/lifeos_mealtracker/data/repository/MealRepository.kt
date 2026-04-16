package com.lifeos.modules.lifeos_mealtracker.data.repository

import com.lifeos.modules.lifeos_mealtracker.data.local.MealEntryDao
import com.lifeos.modules.lifeos_mealtracker.data.local.MealEntryEntity
import com.lifeos.modules.lifeos_mealtracker.data.local.toEntity
import com.lifeos.modules.lifeos_mealtracker.domain.model.DailyTotals
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepository @Inject constructor(
    private val mealEntryDao: MealEntryDao
) {
    fun getMealsByDate(date: LocalDate): Flow<List<MealEntry>> =
        mealEntryDao.getMealsByDate(date.toString()).map { entities ->
            entities.map { it.toDomain() }
        }

    fun getMealsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<MealEntry>> =
        mealEntryDao.getMealsBetweenDates(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }

    fun getAllMeals(): Flow<List<MealEntry>> =
        mealEntryDao.getAllMeals().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getMealById(id: Long): MealEntry? =
        mealEntryDao.getMealById(id)?.toDomain()

    suspend fun insertMeal(meal: MealEntry): Long =
        mealEntryDao.insertMeal(meal.toEntity())

    suspend fun updateMeal(meal: MealEntry) =
        mealEntryDao.updateMeal(meal.toEntity())

    suspend fun deleteMeal(meal: MealEntry) =
        mealEntryDao.deleteMeal(meal.toEntity())

    fun getDailyTotals(date: LocalDate): Flow<DailyTotals> =
        mealEntryDao.getMealsByDate(date.toString()).map { entities ->
            val meals = entities.map { it.toDomain() }
            DailyTotals(
                date = date,
                totalCalories = meals.sumOf { it.calories },
                totalProtein = meals.sumOf { it.protein },
                totalCarbs = meals.sumOf { it.carbs },
                totalFat = meals.sumOf { it.fat },
                meals = meals
            )
        }

    suspend fun deleteMealsByDate(date: LocalDate) =
        mealEntryDao.deleteMealsByDate(date.toString())
}
