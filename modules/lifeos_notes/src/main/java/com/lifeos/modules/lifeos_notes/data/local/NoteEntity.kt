package com.lifeos.modules.lifeos_notes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lifeos_notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val notificationTime: Long? = null
)
