package com.tristinbaker.lifeos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifeos.core.ModuleRegistry
import com.lifeos.modules.lifeos_mealtracker.MealTrackerModule
import com.lifeos.modules.lifeos_notes.NotesModule
import com.lifeos.modules.lifeos_habittracker.HabitsModule
import com.lifeos.modules.lifeos_medialogger.MediaLoggerModule
import com.tristinbaker.lifeos.ui.home.HomeScreen
import com.tristinbaker.lifeos.ui.theme.LifeOSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ModuleRegistry.register(MealTrackerModule())
        ModuleRegistry.register(NotesModule())
        ModuleRegistry.register(HabitsModule())
        ModuleRegistry.register(MediaLoggerModule())

        val initialModule = intent?.getStringExtra("module")
        val initialNoteId = if (initialModule == "notes") {
            intent?.getLongExtra("noteId", -1L)?.takeIf { it != -1L }
        } else null

        val startDestination = if (initialNoteId != null) "notes" else "home"

        enableEdgeToEdge()

        setContent {
            LifeOSTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LifeOSNavHost(
                        startDestination = startDestination,
                        initialNoteId = initialNoteId
                    )
                }
            }
        }
    }
}

@Composable
fun LifeOSNavHost(
    navController: androidx.navigation.NavHostController = rememberNavController(),
    startDestination: String = "home",
    initialNoteId: Long? = null,
    initialHabitId: Long? = null
) {
    var noteIdForNotes by remember { mutableStateOf(initialNoteId) }
    var habitIdForHabits by remember { mutableStateOf(initialHabitId) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("home") {
            HomeScreen(
                onModuleClick = { moduleId ->
                    noteIdForNotes = null
                    navController.navigate(moduleId)
                }
            )
        }

        composable("mealtracker") {
            val module = ModuleRegistry.getModule("mealtracker")
            module?.Content(
                onNavigateBack = { navController.popBackStack() },
                initialId = null
            )
        }

        composable("notes") {
            val module = ModuleRegistry.getModule("notes")
            module?.Content(
                onNavigateBack = { navController.popBackStack() },
                initialId = noteIdForNotes
            )
            LaunchedEffect(Unit) {
                noteIdForNotes = null
            }
        }

        composable("habits") {
            val module = ModuleRegistry.getModule("habits")
            module?.Content(
                onNavigateBack = { navController.popBackStack() },
                initialId = habitIdForHabits
            )
            LaunchedEffect(Unit) {
                habitIdForHabits = null
            }
        }

        composable("medialogger") {
            val module = ModuleRegistry.getModule("medialogger")
            module?.Content(
                onNavigateBack = { navController.popBackStack() },
                initialId = null
            )
        }
    }
}
