package com.lifeos.modules.lifeos_notes.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_notes.data.local.NoteEntity
import com.lifeos.modules.lifeos_notes.data.repository.NotesRepository
import com.lifeos.modules.lifeos_notes.service.NotesNotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllNotes().collect { notes ->
                _uiState.value = NotesUiState(notes = notes, isLoading = false)
            }
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.updatePinStatus(note.id, !note.isPinned)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun saveNote(
        id: Long?,
        title: String,
        content: String,
        checklistJson: String,
        isPinned: Boolean,
        notificationTime: Long?,
        context: Context
    ) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            
            val note = NoteEntity(
                id = id ?: 0,
                title = title,
                content = content,
                checklistJson = checklistJson,
                isPinned = isPinned,
                createdAt = if (id != null) repository.getNoteById(id)?.createdAt ?: currentTime else currentTime,
                updatedAt = currentTime,
                notificationTime = notificationTime
            )

            val noteId = repository.insertNote(note)

            if (notificationTime != null) {
                NotesNotificationHelper.scheduleNotification(
                    context = context,
                    noteId = noteId,
                    title = title,
                    content = content,
                    triggerTime = notificationTime
                )
            } else {
                NotesNotificationHelper.cancelNotification(context, noteId)
            }
        }
    }

    fun deleteNoteById(id: Long, context: Context) {
        viewModelScope.launch {
            NotesNotificationHelper.cancelNotification(context, id)
            repository.deleteNoteById(id)
        }
    }
}
