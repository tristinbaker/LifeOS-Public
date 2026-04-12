package com.lifeos.modules.lifeos_notes.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM lifeos_notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM lifeos_notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("SELECT * FROM lifeos_notes WHERE id = :id")
    fun getNoteByIdFlow(id: Long): Flow<NoteEntity?>

    @Query("SELECT * FROM lifeos_notes WHERE notificationTime IS NOT NULL AND notificationTime > :currentTime")
    suspend fun getNotesWithUpcomingNotifications(currentTime: Long): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM lifeos_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("UPDATE lifeos_notes SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePinStatus(id: Long, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE lifeos_notes SET notificationTime = :notificationTime, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateNotificationTime(id: Long, notificationTime: Long?, updatedAt: Long = System.currentTimeMillis())
}
