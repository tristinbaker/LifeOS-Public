package com.lifeos.modules.lifeos_habittracker.ui.habits

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lifeos.modules.lifeos_habittracker.data.local.HabitFrequency
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditorScreen(
    habit: com.lifeos.modules.lifeos_habittracker.data.local.HabitEntity?,
    onNavigateBack: () -> Unit,
    onSave: (
        name: String,
        description: String,
        frequency: HabitFrequency,
        daysOfWeek: String,
        timesPerWeek: Int,
        reminderEnabled: Boolean,
        reminderTime: Long?,
        onComplete: () -> Unit
    ) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    var name by remember(habit) { mutableStateOf(habit?.name ?: "") }
    var description by remember(habit) { mutableStateOf(habit?.description ?: "") }
    var frequency by remember(habit) { mutableStateOf(habit?.frequency ?: HabitFrequency.DAILY) }
    var daysOfWeek by remember(habit) { mutableStateOf(habit?.daysOfWeek ?: "1,2,3,4,5,6,7") }
    var timesPerWeek by remember(habit) { mutableStateOf(habit?.timesPerWeek ?: 3) }
    var reminderEnabled by remember(habit) { mutableStateOf(habit?.reminderEnabled ?: false) }
    var reminderTime by remember(habit) { mutableStateOf(habit?.reminderTime) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var showTimesPerWeekDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            reminderEnabled = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = { Text(if (habit == null) "New Habit" else "Edit Habit") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                if (habit != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Habit name") },
                placeholder = { Text("e.g., Exercise, Read, Meditate") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Description (optional)") },
                placeholder = { Text("Add details...") },
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Frequency",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(Modifier.selectableGroup()) {
                FrequencyOption(
                    text = "Daily",
                    selected = frequency == HabitFrequency.DAILY,
                    onClick = { frequency = HabitFrequency.DAILY }
                )
                FrequencyOption(
                    text = "Specific days",
                    selected = frequency == HabitFrequency.SPECIFIC_DAYS,
                    onClick = { frequency = HabitFrequency.SPECIFIC_DAYS }
                )
                FrequencyOption(
                    text = "Times per week",
                    selected = frequency == HabitFrequency.TIMES_PER_WEEK,
                    onClick = { frequency = HabitFrequency.TIMES_PER_WEEK }
                )
            }

            if (frequency == HabitFrequency.SPECIFIC_DAYS) {
                Spacer(modifier = Modifier.height(16.dp))
                DaySelector(
                    selectedDays = daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet(),
                    onDaysChanged = { days ->
                        daysOfWeek = days.sorted().joinToString(",")
                    }
                )
            }

            if (frequency == HabitFrequency.TIMES_PER_WEEK) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showTimesPerWeekDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("$timesPerWeek times per week")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Reminder",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!reminderEnabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    reminderEnabled = !reminderEnabled
                                } else {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                reminderEnabled = !reminderEnabled
                            }
                        } else {
                            reminderEnabled = false
                        }
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (reminderEnabled) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Daily reminder",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (reminderEnabled && reminderTime != null) {
                        val dt = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(reminderTime!!),
                            ZoneId.systemDefault()
                        )
                        Text(
                            dt.format(DateTimeFormatter.ofPattern("h:mm a")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    reminderEnabled = true
                                } else {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                reminderEnabled = true
                            }
                        } else {
                            reminderEnabled = false
                        }
                    }
                )
            }

            if (reminderEnabled) {
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    if (reminderTime != null) {
                        val dt = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(reminderTime!!),
                            ZoneId.systemDefault()
                        )
                        Text(dt.format(DateTimeFormatter.ofPattern("h:mm a")))
                    } else {
                        Text("Set time")
                    }
                }
            }
        }

        Button(
            onClick = {
                onSave(
                    name,
                    description,
                    frequency,
                    daysOfWeek,
                    timesPerWeek,
                    reminderEnabled,
                    reminderTime,
                    onNavigateBack
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = name.isNotBlank()
        ) {
            Text(if (habit == null) "Create Habit" else "Save Changes")
        }
    }

    if (showTimePicker) {
        val currentTime = reminderTime?.let {
            val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            Pair(dt.hour, dt.minute)
        } ?: Pair(9, 0)
        
        val timePickerState = rememberTimePickerState(currentTime.first, currentTime.second)
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Reminder Time") },
            text = { TimePicker(timePickerState) },
            confirmButton = {
                TextButton({
                    val now = LocalDateTime.now()
                    val selected = now.withHour(timePickerState.hour).withMinute(timePickerState.minute)
                    reminderTime = selected.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    showTimePicker = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton({ showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    if (showTimesPerWeekDialog) {
        var selectedTimes by remember { mutableIntStateOf(timesPerWeek) }
        
        AlertDialog(
            onDismissRequest = { showTimesPerWeekDialog = false },
            title = { Text("Times per week") },
            text = {
                Column {
                    (1..7).forEach { times ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTimes = times }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTimes == times,
                                onClick = { selectedTimes = times }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("$times ${if (times == 1) "time" else "times"} per week")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton({
                    timesPerWeek = selectedTimes
                    showTimesPerWeekDialog = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton({ showTimesPerWeekDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FrequencyOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(Modifier.width(12.dp))
        Text(text)
    }
}

@Composable
private fun DaySelector(
    selectedDays: Set<Int>,
    onDaysChanged: (List<Int>) -> Unit
) {
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dayNames.forEachIndexed { index, day ->
            val dayNumber = index + 1
            val isSelected = selectedDays.contains(dayNumber)
            
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newDays = if (isSelected) {
                        selectedDays - dayNumber
                    } else {
                        selectedDays + dayNumber
                    }
                    onDaysChanged(newDays.toList())
                },
                label = { Text(day) },
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}
