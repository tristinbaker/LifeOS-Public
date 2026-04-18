package com.lifeos.modules.lifeos_mealtracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lifeos.modules.lifeos_mealtracker.domain.model.StoredItem

@Entity(tableName = "lifeos_mealtracker_stored_items")
data class StoredItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val caloriesPerUnit: Int,
    val proteinPerUnit: Int,
    val carbsPerUnit: Int,
    val fatPerUnit: Int
) {
    fun toDomain() = StoredItem(id, name, caloriesPerUnit, proteinPerUnit, carbsPerUnit, fatPerUnit)
}

fun StoredItem.toEntity() = StoredItemEntity(id, name, caloriesPerUnit, proteinPerUnit, carbsPerUnit, fatPerUnit)
