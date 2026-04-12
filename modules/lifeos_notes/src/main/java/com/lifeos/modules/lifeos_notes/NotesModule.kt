package com.lifeos.modules.lifeos_notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lifeos.core.LifeOSModule
import com.lifeos.modules.lifeos_notes.service.NotesNotificationHelper
import com.lifeos.modules.lifeos_notes.ui.NotesViewModel
import com.lifeos.modules.lifeos_notes.ui.notes.NoteEditorScreen
import com.lifeos.modules.lifeos_notes.ui.notes.NotesListScreen

class NotesModule : LifeOSModule {
    override val id: String = "notes"
    override val name: String = "Notes"
    override val icon: ImageVector = Icons.Default.Note
    override val description: String = "Keep track of things with notes and checklists"
    override val version: String = "1.0"

    @Composable
    override fun Content(
        onNavigateBack: () -> Unit,
        initialId: Long?
    ) {
        val moduleNavController = rememberNavController()
        val context = LocalContext.current
        val viewModel: NotesViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(Unit) {
            NotesNotificationHelper.createNotificationChannel(context)
        }

        LaunchedEffect(initialId) {
            initialId?.let { noteId ->
                moduleNavController.navigate("edit/$noteId")
            }
        }

        NavHost(
            navController = moduleNavController,
            startDestination = "list"
        ) {
            composable("list") {
                BackHandler(onBack = onNavigateBack)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        TextButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Back to LifeOS")
                        }
                    }

                    NotesListScreen(
                        notes = uiState.notes,
                        onNoteClick = { noteId ->
                            moduleNavController.navigate("edit/$noteId")
                        },
                        onCreateNote = {
                            moduleNavController.navigate("create")
                        },
                        onPinNote = { note ->
                            viewModel.togglePin(note)
                        },
                        onDeleteNote = { note ->
                            viewModel.deleteNote(note)
                        }
                    )
                }
            }

            composable("create") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                NoteEditorScreen(
                    note = null,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { title, content, checklistJson, isPinned, notificationTime ->
                        viewModel.saveNote(
                            id = null,
                            title = title,
                            content = content,
                            checklistJson = checklistJson,
                            isPinned = isPinned,
                            notificationTime = notificationTime,
                            context = context
                        )
                    },
                    onDelete = { moduleNavController.popBackStack() }
                )
            }

            composable(
                route = "edit/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getLong("noteId")
                val note = uiState.notes.find { it.id == noteId }

                BackHandler(onBack = { moduleNavController.popBackStack() })
                NoteEditorScreen(
                    note = note,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { title, content, checklistJson, isPinned, notificationTime ->
                        viewModel.saveNote(
                            id = noteId,
                            title = title,
                            content = content,
                            checklistJson = checklistJson,
                            isPinned = isPinned,
                            notificationTime = notificationTime,
                            context = context
                        )
                    },
                    onDelete = {
                        noteId?.let {
                            viewModel.deleteNoteById(it, context)
                        }
                        moduleNavController.popBackStack("list", inclusive = false)
                    }
                )
            }
        }
    }
}

@Composable
private fun BackHandler(onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = onBack)
}
