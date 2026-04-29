package com.lifeos.modules.lifeos_journal

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lifeos.core.LifeOSModule
import com.lifeos.modules.lifeos_journal.ui.JournalViewModel
import com.lifeos.modules.lifeos_journal.ui.journal.JournalEditorScreen
import com.lifeos.modules.lifeos_journal.ui.journal.JournalListScreen
import com.lifeos.modules.lifeos_journal.ui.journal.JournalSettingsScreen
import com.lifeos.modules.lifeos_journal.ui.journal.WeekReviewScreen

class JournalModule : LifeOSModule {
    override val id: String = "journal"
    override val name: String = "Journal"
    override val icon: ImageVector = Icons.Default.Book
    override val description: String = "Daily journaling and reflection"
    override val shortDescription: String = "Daily journaling"
    override val version: String = "1.0"

    @Composable
    override fun Content(
        onNavigateBack: () -> Unit,
        initialId: Long?
    ) {
        val moduleNavController = rememberNavController()
        val viewModel: JournalViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(initialId) {
            initialId?.let { entryId ->
                moduleNavController.navigate("edit/$entryId")
            }
        }

        NavHost(
            navController = moduleNavController,
            startDestination = "list"
        ) {
            composable("list") {
                BackHandler(onBack = onNavigateBack)
                LaunchedEffect(Unit) {
                    viewModel.loadImagesForEntry(null)
                    viewModel.loadWeeklyImages()
                }
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

                    JournalListScreen(
                        entries = uiState.entries,
                        journalStats = uiState.journalStats,
                        onEntryClick = { entryId ->
                            moduleNavController.navigate("edit/$entryId")
                        },
                        onAddClick = {
                            moduleNavController.navigate("create")
                        },
                        onSettingsClick = {
                            moduleNavController.navigate("settings")
                        },
                        onWeekReviewClick = {
                            moduleNavController.navigate("week_review")
                        }
                    )
                }
            }

            composable("create") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                LaunchedEffect(Unit) {
                    viewModel.loadImagesForEntry(null)
                }
                JournalEditorScreen(
                    entry = null,
                    images = emptyList(),
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, date, content, mood, newImageUris, removedImageIds, onComplete ->
                        viewModel.saveEntry(
                            id = id,
                            date = date,
                            content = content,
                            mood = mood,
                            newImageUris = newImageUris,
                            removedImageIds = removedImageIds,
                            onComplete = onComplete
                        )
                    },
                    onDelete = { moduleNavController.popBackStack() }
                )
            }

            composable(
                route = "edit/{entryId}",
                arguments = listOf(navArgument("entryId") { type = NavType.LongType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getLong("entryId")
                val entry = uiState.entries.find { it.id == entryId }

                LaunchedEffect(entryId) {
                    viewModel.loadImagesForEntry(entryId)
                }

                BackHandler(onBack = { moduleNavController.popBackStack() })
                JournalEditorScreen(
                    entry = entry,
                    images = uiState.currentEntryImages,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, date, content, mood, newImageUris, removedImageIds, onComplete ->
                        viewModel.saveEntry(
                            id = id,
                            date = date,
                            content = content,
                            mood = mood,
                            newImageUris = newImageUris,
                            removedImageIds = removedImageIds,
                            onComplete = onComplete
                        )
                    },
                    onDelete = {
                        entryId?.let {
                            viewModel.deleteEntry(it) {
                                moduleNavController.popBackStack("list", inclusive = false)
                            }
                        }
                    }
                )
            }

            composable("week_review") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                WeekReviewScreen(
                    images = uiState.weeklyImages,
                    weekLabel = uiState.weekLabel,
                    weekOffset = uiState.weekOffset,
                    isLoading = uiState.weekReviewLoading,
                    onPrevWeek = { viewModel.navigateWeek(-1) },
                    onNextWeek = { viewModel.navigateWeek(1) },
                    onClose = { moduleNavController.popBackStack() }
                )
            }

            composable("settings") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                JournalSettingsScreen(
                    reminderEnabled = uiState.settings?.reminderEnabled ?: false,
                    reminderTime = uiState.settings?.reminderTime ?: (21 * 60 * 60 * 1000),
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onUpdateSettings = { enabled, time ->
                        viewModel.updateSettings(enabled, time)
                    }
                )
            }
        }
    }
}
