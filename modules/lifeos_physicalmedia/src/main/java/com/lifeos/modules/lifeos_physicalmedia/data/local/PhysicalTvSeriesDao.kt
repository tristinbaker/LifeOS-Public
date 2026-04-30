package com.lifeos.modules.lifeos_physicalmedia.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PhysicalTvSeriesDao {
    @Query("SELECT * FROM physical_tv_series ORDER BY title ASC")
    fun getAll(): Flow<List<PhysicalTvSeriesEntity>>

    @Query("SELECT * FROM physical_tv_series WHERE id = :id")
    suspend fun getById(id: Long): PhysicalTvSeriesEntity?

    @Insert
    suspend fun insert(entity: PhysicalTvSeriesEntity): Long

    @Update
    suspend fun update(entity: PhysicalTvSeriesEntity)

    @Query("DELETE FROM physical_tv_series WHERE id = :id")
    suspend fun deleteById(id: Long)
}
