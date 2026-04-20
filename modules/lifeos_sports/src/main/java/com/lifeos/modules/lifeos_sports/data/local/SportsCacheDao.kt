package com.lifeos.modules.lifeos_sports.data.local

import androidx.room.*

@Dao
interface SportsCacheDao {
    @Query("SELECT * FROM sports_cache WHERE `key` = :key")
    suspend fun get(key: String): SportsCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SportsCacheEntity)

    @Query("DELETE FROM sports_cache WHERE `key` = :key")
    suspend fun deleteByKey(key: String)
}
