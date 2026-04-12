package com.lifeos.modules.lifeos_mealtracker.data.repository

import com.lifeos.modules.lifeos_mealtracker.data.local.SavedMealDao
import com.lifeos.modules.lifeos_mealtracker.data.local.SavedMealEntity
import com.lifeos.modules.lifeos_mealtracker.data.local.toEntity
import com.lifeos.modules.lifeos_mealtracker.domain.model.SavedMeal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedMealRepository @Inject constructor(
    private val savedMealDao: SavedMealDao
) {
    fun getAllSavedMeals(): Flow<List<SavedMeal>> =
        savedMealDao.getAllSavedMeals().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getSavedMealById(id: Long): SavedMeal? =
        savedMealDao.getSavedMealById(id)?.toDomain()

    suspend fun insertSavedMeal(meal: SavedMeal): Long =
        savedMealDao.insertSavedMeal(meal.toEntity())

    suspend fun updateSavedMeal(meal: SavedMeal) =
        savedMealDao.updateSavedMeal(meal.toEntity())

    suspend fun deleteSavedMeal(meal: SavedMeal) =
        savedMealDao.deleteSavedMeal(meal.toEntity())
}
