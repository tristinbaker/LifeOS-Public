package com.lifeos.modules.lifeos_journal

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

class JournalModule : LifeOSModule {
    override val id: String = "journal"
    override val name: String = "Journal"
    override val icon: ImageVector = Icons.Default.Book
    override val description: String = "Daily journaling and reflection"
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
                        weeklyStats = uiState.weeklyStats,
                        onEntryClick = { entryId ->
                            moduleNavController.navigate("edit/$entryId")
                        },
                        onAddClick = {
                            moduleNavController.navigate("create")
                        },
                        onSettingsClick = {
                            moduleNavController.navigate("settings")
                        }
                    )
                }
            }

            composable("create") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                JournalEditorScreen(
                    entry = null,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, date, content, mood, onComplete ->
                        viewModel.saveEntry(
                            id = id,
                            date = date,
                            content = content,
                            mood = mood,
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

                BackHandler(onBack = { moduleNavController.popBackStack() })
                JournalEditorScreen(
                    entry = entry,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, date, content, mood, onComplete ->
                        viewModel.saveEntry(
                            id = id,
                            date = date,
                            content = content,
                            mood = mood,
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