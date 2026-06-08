package com.lifeos.modules.lifeos_habittracker

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
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
import com.lifeos.modules.lifeos_habittracker.ui.HabitsViewModel
import com.lifeos.modules.lifeos_habittracker.ui.habits.HabitEditorScreen
import com.lifeos.modules.lifeos_habittracker.ui.habits.HabitsListScreen

class HabitsModule : LifeOSModule {
    override val id: String = "habits"
    override val name: String = "Habits"
    override val icon: ImageVector = Icons.Default.FitnessCenter
    override val description: String = "Track your daily habits and build streaks"
    override val shortDescription: String = "Habits & streaks"
    override val version: String = "1.0"

    @Composable
    override fun Content(
        onNavigateBack: () -> Unit,
        initialId: Long?
    ) {
        val moduleNavController = rememberNavController()
        val viewModel: HabitsViewModel = hiltViewModel()
        val habitsWithStats by viewModel.habitsWithStats.collectAsState()

        LaunchedEffect(initialId) {
            initialId?.let { habitId ->
                moduleNavController.navigate("edit/$habitId")
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

                    HabitsListScreen(
                        habitsWithStats = habitsWithStats,
                        onToggleCheckIn = { habitId ->
                            viewModel.toggleCheckIn(habitId)
                        },
                        onToggleCheckInForDate = { habitId, date ->
                            viewModel.toggleCheckInForDate(habitId, date)
                        },
                        onHabitClick = { habitId ->
                            moduleNavController.navigate("edit/$habitId")
                        },
                        onAddClick = {
                            moduleNavController.navigate("create")
                        }
                    )
                }
            }

            composable("create") {
                BackHandler(onBack = { moduleNavController.popBackStack() })
                HabitEditorScreen(
                    habit = null,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { name, description, frequency, daysOfWeek, timesPerWeek, reminderEnabled, reminderTime, reminderDays, onComplete ->
                        viewModel.saveHabit(
                            id = null,
                            name = name,
                            description = description,
                            frequency = frequency,
                            daysOfWeek = daysOfWeek,
                            timesPerWeek = timesPerWeek,
                            reminderEnabled = reminderEnabled,
                            reminderTime = reminderTime,
                            reminderDays = reminderDays,
                            onComplete = onComplete
                        )
                    },
                    onDelete = { moduleNavController.popBackStack() }
                )
            }

            composable(
                route = "edit/{habitId}",
                arguments = listOf(navArgument("habitId") { type = NavType.LongType })
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getLong("habitId")
                val habit = habitsWithStats.find { it.habit.id == habitId }?.habit

                BackHandler(onBack = { moduleNavController.popBackStack() })
                HabitEditorScreen(
                    habit = habit,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onSave = { name, description, frequency, daysOfWeek, timesPerWeek, reminderEnabled, reminderTime, reminderDays, onComplete ->
                        viewModel.saveHabit(
                            id = habitId,
                            name = name,
                            description = description,
                            frequency = frequency,
                            daysOfWeek = daysOfWeek,
                            timesPerWeek = timesPerWeek,
                            reminderEnabled = reminderEnabled,
                            reminderTime = reminderTime,
                            reminderDays = reminderDays,
                            onComplete = onComplete
                        )
                    },
                    onDelete = {
                        habitId?.let {
                            viewModel.deleteHabit(it) {
                                moduleNavController.popBackStack("list", inclusive = false)
                            }
                        }
                    }
                )
            }
        }
    }
}
