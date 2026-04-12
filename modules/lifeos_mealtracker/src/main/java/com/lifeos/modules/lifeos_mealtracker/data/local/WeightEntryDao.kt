package com.lifeos.modules.lifeos_mealtracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WeightEntryDao {
    @Query("SELECT * FROM lifeos_mealtracker_weight_entries ORDER BY date DESC")
    fun getAllWeightEntries(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM lifeos_mealtracker_weight_entries WHERE date = :date")
    suspend fun getWeightEntryByDate(date: LocalDate): WeightEntryEntity?

    @Query("SELECT * FROM lifeos_mealtracker_weight_entries ORDER BY date DESC LIMIT 1")
    fun getLatestWeightEntry(): Flow<WeightEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(entry: WeightEntryEntity): Long

    @Update
    suspend fun updateWeightEntry(entry: WeightEntryEntity)

    @Delete
    suspend fun deleteWeightEntry(entry: WeightEntryEntity)
}
