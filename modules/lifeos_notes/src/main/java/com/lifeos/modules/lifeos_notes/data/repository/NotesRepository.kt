package com.lifeos.modules.lifeos_notes.data.repository

import com.lifeos.modules.lifeos_notes.data.local.NoteDao
import com.lifeos.modules.lifeos_notes.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    fun getNoteByIdFlow(id: Long): Flow<NoteEntity?> = noteDao.getNoteByIdFlow(id)

    suspend fun getNotesWithUpcomingNotifications(currentTime: Long): List<NoteEntity> =
        noteDao.getNotesWithUpcomingNotifications(currentTime)

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    suspend fun updatePinStatus(id: Long, isPinned: Boolean) = noteDao.updatePinStatus(id, isPinned)

    suspend fun updateNotificationTime(id: Long, notificationTime: Long?) =
        noteDao.updateNotificationTime(id, notificationTime)
}
