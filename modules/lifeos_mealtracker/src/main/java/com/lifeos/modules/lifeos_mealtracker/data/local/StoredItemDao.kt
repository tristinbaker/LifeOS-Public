package com.lifeos.modules.lifeos_mealtracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StoredItemDao {
    @Query("SELECT * FROM lifeos_mealtracker_stored_items ORDER BY name ASC")
    fun getAllStoredItems(): Flow<List<StoredItemEntity>>

    @Query("SELECT * FROM lifeos_mealtracker_stored_items WHERE id = :id")
    suspend fun getStoredItemById(id: Long): StoredItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoredItem(item: StoredItemEntity): Long

    @Update
    suspend fun updateStoredItem(item: StoredItemEntity)

    @Delete
    suspend fun deleteStoredItem(item: StoredItemEntity)

    @Query("SELECT * FROM lifeos_mealtracker_stored_items WHERE barcode = :barcode LIMIT 1")
    suspend fun getStoredItemByBarcode(barcode: String): StoredItemEntity?
}
