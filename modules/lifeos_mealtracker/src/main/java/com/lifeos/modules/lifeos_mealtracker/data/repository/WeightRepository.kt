package com.lifeos.modules.lifeos_mealtracker.data.repository

import com.lifeos.modules.lifeos_mealtracker.data.local.WeightEntryDao
import com.lifeos.modules.lifeos_mealtracker.data.local.WeightEntryEntity
import com.lifeos.modules.lifeos_mealtracker.data.local.toEntity
import com.lifeos.modules.lifeos_mealtracker.domain.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepository @Inject constructor(
    private val weightEntryDao: WeightEntryDao
) {
    fun getAllWeightEntries(): Flow<List<WeightEntry>> =
        weightEntryDao.getAllWeightEntries().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getWeightEntryByDate(date: LocalDate): WeightEntry? =
        weightEntryDao.getWeightEntryByDate(date)?.toDomain()

    fun getLatestWeightEntry(): Flow<WeightEntry?> =
        weightEntryDao.getLatestWeightEntry().map { it?.toDomain() }

    suspend fun insertWeightEntry(entry: WeightEntry): Long =
        weightEntryDao.insertWeightEntry(entry.toEntity())

    suspend fun updateWeightEntry(entry: WeightEntry) =
        weightEntryDao.updateWeightEntry(entry.toEntity())

    suspend fun deleteWeightEntry(entry: WeightEntry) =
        weightEntryDao.deleteWeightEntry(entry.toEntity())
}
