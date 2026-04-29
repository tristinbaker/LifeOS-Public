package com.lifeos.modules.lifeos_journal.ui.journal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalSettingsScreen(
    reminderEnabled: Boolean,
    reminderTime: Long,
    onNavigateBack: () -> Unit,
    onUpdateSettings: (enabled: Boolean, time: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var enabled by remember(reminderEnabled) { mutableStateOf(reminderEnabled) }
    val sanitizedTime = reminderTime.takeIf { it in 0..(23L * 60 * 60 * 1000 + 59 * 60 * 1000) }
        ?: (21L * 60 * 60 * 1000)
    var time by remember(reminderTime) { mutableStateOf(sanitizedTime) }
    var showTimePicker by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enabled = true
            onUpdateSettings(true, time)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = { Text("Journal Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context, Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPermission) {
                                            enabled = true
                                            onUpdateSettings(true, time)
                                        } else {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    } else {
                                        enabled = true
                                        onUpdateSettings(true, time)
                                    }
                                } else {
                                    enabled = false
                                    onUpdateSettings(false, time)
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Daily Reminder",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Get reminded to write in your journal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = enabled,
                            onCheckedChange = { isEnabled ->
                                if (isEnabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context, Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPermission) {
                                            enabled = true
                                            onUpdateSettings(true, time)
                                        } else {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    } else {
                                        enabled = true
                                        onUpdateSettings(true, time)
                                    }
                                } else {
                                    enabled = false
                                    onUpdateSettings(false, time)
                                }
                            }
                        )
                    }

                    if (enabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Reminder Time",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            val lt = LocalTime.of(
                                (time / (60 * 60 * 1000)).toInt(),
                                ((time / (60 * 1000)) % 60).toInt()
                            )
                            Text(lt.format(DateTimeFormatter.ofPattern("h:mm a")))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Get a daily prompt to reflect on your day and write in your journal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            (time / (60 * 60 * 1000)).toInt(),
            ((time / (60 * 1000)) % 60).toInt()
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Reminder Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton({
                    time = (timePickerState.hour * 60L * 60 * 1000) + (timePickerState.minute * 60L * 1000)
                    onUpdateSettings(enabled, time)
                    showTimePicker = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton({ showTimePicker = false }) { Text("Cancel") }
            }
        )
    }
}