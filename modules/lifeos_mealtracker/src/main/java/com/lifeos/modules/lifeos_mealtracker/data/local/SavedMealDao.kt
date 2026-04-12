package com.lifeos.modules.lifeos_mealtracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedMealDao {
    @Query("SELECT * FROM lifeos_mealtracker_saved_meals ORDER BY name ASC")
    fun getAllSavedMeals(): Flow<List<SavedMealEntity>>

    @Query("SELECT * FROM lifeos_mealtracker_saved_meals WHERE id = :id")
    suspend fun getSavedMealById(id: Long): SavedMealEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedMeal(meal: SavedMealEntity): Long

    @Update
    suspend fun updateSavedMeal(meal: SavedMealEntity)

    @Delete
    suspend fun deleteSavedMeal(meal: SavedMealEntity)
}
