package com.lifeos.modules.lifeos_mealtracker.data.repository

import com.lifeos.modules.lifeos_mealtracker.data.local.StoredItemDao
import com.lifeos.modules.lifeos_mealtracker.data.local.StoredItemEntity
import com.lifeos.modules.lifeos_mealtracker.data.local.toEntity
import com.lifeos.modules.lifeos_mealtracker.domain.model.StoredItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoredItemRepository @Inject constructor(
    private val storedItemDao: StoredItemDao
) {
    fun getAllStoredItems(): Flow<List<StoredItem>> =
        storedItemDao.getAllStoredItems().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getStoredItemById(id: Long): StoredItem? =
        storedItemDao.getStoredItemById(id)?.toDomain()

    suspend fun insertStoredItem(item: StoredItem): Long =
        storedItemDao.insertStoredItem(item.toEntity())

    suspend fun updateStoredItem(item: StoredItem) =
        storedItemDao.updateStoredItem(item.toEntity())

    suspend fun deleteStoredItem(item: StoredItem) =
        storedItemDao.deleteStoredItem(item.toEntity())

    suspend fun getStoredItemByBarcode(barcode: String): StoredItem? =
        storedItemDao.getStoredItemByBarcode(barcode)?.toDomain()
}
