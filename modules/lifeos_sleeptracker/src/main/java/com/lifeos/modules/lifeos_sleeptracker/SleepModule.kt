package com.lifeos.modules.lifeos_sleeptracker

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
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
import com.lifeos.modules.lifeos_sleeptracker.ui.SleepViewModel
import com.lifeos.modules.lifeos_sleeptracker.ui.sleeptracker.SleepEditorScreen
import com.lifeos.modules.lifeos_sleeptracker.ui.sleeptracker.SleepListScreen
import com.lifeos.modules.lifeos_sleeptracker.ui.sleeptracker.SleepSettingsScreen

class SleepModule : LifeOSModule {
    override val id: String = "sleep"
    override val name: String = "Sleep"
    override val icon: ImageVector = Icons.Default.Bedtime
    override val description: String = "Track your sleep patterns and quality"
    override val shortDescription: String = "Sleep patterns & quality"
    override val version: String = "1.0"

    @Composable
    override fun Content(
        onNavigateBack: () -> Unit,
        initialId: Long?
    ) {
        val moduleNavController = rememberNavController()
        val viewModel: SleepViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(initialId) {
            initialId?.let { logId ->
                moduleNavController.navigate("edit/$logId")
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

                    SleepListScreen(
                        logs = uiState.logs,
                        weeklyStats = uiState.weeklyStats,
                        onLogClick = { logId ->
                            moduleNavController.navigate("edit/$logId")
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
                SleepEditorScreen(
                    log = null,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, date, startTime, endTime, quality, dreamNotes, notes, sleepMedicationTaken, onComplete ->
                        viewModel.saveLog(
                            id = id,
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            quality = quality,
                            dreamNotes = dreamNotes,
                            notes = notes,
                            sleepMedicationTaken = sleepMedicationTaken,
                            onComplete = onComplete
                        )
                    },
                    onDelete = { moduleNavController.popBackStack() }
                )
            }

            composable(
                route = "edit/{logId}",
                arguments = listOf(navArgument("logId") { type = NavType.LongType })
            ) { backStackEntry ->
                val logId = backStackEntry.arguments?.getLong("logId")
                val log = uiState.logs.find { it.log.id == logId }?.log

                BackHandler(onBack = { moduleNavController.popBackStack() })
                SleepEditorScreen(
                    log = log,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { id, date, startTime, endTime, quality, dreamNotes, notes, sleepMedicationTaken, onComplete ->
                        viewModel.saveLog(
                            id = id,
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            quality = quality,
                            dreamNotes = dreamNotes,
                            notes = notes,
                            sleepMedicationTaken = sleepMedicationTaken,
                            onComplete = onComplete
                        )
                    },
                    onDelete = {
                        logId?.let {
                            viewModel.deleteLog(it) {
                                moduleNavController.popBackStack("list", inclusive = false)
                            }
                        }
                    }
                )
            }

            composable("settings") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                SleepSettingsScreen(
                    reminderEnabled = uiState.settings?.reminderEnabled ?: false,
                    reminderTime = uiState.settings?.reminderTime ?: (22 * 60 * 60 * 1000),
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onUpdateSettings = { enabled, time ->
                        viewModel.updateSettings(enabled, time)
                    }
                )
            }
        }
    }
}