package com.tristinbaker.lifeos

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lifeos.core.ModuleRegistry
import com.lifeos.modules.lifeos_mealtracker.MealTrackerModule
import com.lifeos.modules.lifeos_notes.NotesModule
import com.lifeos.modules.lifeos_habittracker.HabitsModule
import com.lifeos.modules.lifeos_medialogger.MediaLoggerModule
import com.lifeos.modules.lifeos_sleeptracker.SleepModule
import com.lifeos.modules.lifeos_journal.JournalModule
import com.tristinbaker.lifeos.backup.BackupPreferences
import com.tristinbaker.lifeos.ui.home.HomeScreen
import com.tristinbaker.lifeos.ui.theme.LifeOSTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ModuleRegistry.register(MealTrackerModule())
        ModuleRegistry.register(NotesModule())
        ModuleRegistry.register(HabitsModule())
        ModuleRegistry.register(MediaLoggerModule())
        ModuleRegistry.register(SleepModule())
        ModuleRegistry.register(JournalModule())

        val initialModule = intent?.getStringExtra("module")
        val initialNoteId = if (initialModule == "notes") {
            intent?.getLongExtra("noteId", -1L)?.takeIf { it != -1L }
        } else null

        val startDestination = when {
            initialNoteId != null -> "notes"
            initialModule == "habits" -> "habits"
            else -> "home"
        }

        // Read biometric setting before setContent to avoid any flash of unlocked state
        val startLocked = runBlocking { BackupPreferences.biometricEnabled(this@MainActivity).first() }

        enableEdgeToEdge()

        setContent {
            LifeOSTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val biometricEnabled by BackupPreferences.biometricEnabled(this@MainActivity)
                        .collectAsStateWithLifecycle(initialValue = startLocked)
                    var isAuthenticated by remember { mutableStateOf(!startLocked) }

                    if (biometricEnabled && !isAuthenticated) {
                        LockScreen(
                            onAuthenticate = {
                                showBiometricPrompt { isAuthenticated = true }
                            }
                        )
                    } else {
                        LifeOSNavHost(
                            startDestination = startDestination,
                            initialNoteId = initialNoteId
                        )
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock LifeOS")
            .setSubtitle("Use your biometric credential to continue")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun LockScreen(onAuthenticate: () -> Unit) {
    LaunchedEffect(Unit) { onAuthenticate() }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text("LifeOS is locked", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAuthenticate) {
            Text("Authenticate")
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

        composable("sleep") {
            val module = ModuleRegistry.getModule("sleep")
            module?.Content(
                onNavigateBack = { navController.popBackStack() },
                initialId = null
            )
        }

        composable("journal") {
            val module = ModuleRegistry.getModule("journal")
            module?.Content(
                onNavigateBack = { navController.popBackStack() },
                initialId = null
            )
        }
    }
}
