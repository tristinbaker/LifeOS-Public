package com.lifeos.modules.lifeos_sleeptracker.ui.sleeptracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepEditorScreen(
    log: com.lifeos.modules.lifeos_sleeptracker.data.local.SleepLogEntity?,
    onNavigateBack: () -> Unit,
    onSave: (
        id: Long?,
        date: String,
        startTime: Long,
        endTime: Long,
        quality: Float,
        dreamNotes: String,
        notes: String,
        sleepMedicationTaken: Boolean,
        onComplete: () -> Unit
    ) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var date by remember(log) { 
        mutableStateOf(log?.date ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) 
    }
    var startTimeMillis by remember(log) { 
        mutableStateOf(
            log?.startTime ?: LocalDateTime.now()
                .minusDays(1)
                .withHour(22).withMinute(0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
    }
    var endTimeMillis by remember(log) { 
        mutableStateOf(
            log?.endTime ?: LocalDateTime.now()
                .withHour(6).withMinute(0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
    }
    var quality by remember(log) { mutableStateOf(log?.quality ?: 3f) }
    var dreamNotes by remember(log) { mutableStateOf(log?.dreamNotes ?: "") }
    var notes by remember(log) { mutableStateOf(log?.notes ?: "") }
    var sleepMedicationTaken by remember(log) { mutableStateOf(log?.sleepMedicationTaken ?: false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val parsedDate = try { LocalDate.parse(date) } catch (e: Exception) { LocalDate.now() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = { Text(if (log == null) "Log Sleep" else "Edit Sleep") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                if (log != null) {
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
            Text(
                "Date",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "${parsedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())}, ${
                        parsedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    }"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Sleep Times",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showStartTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Bedtime", style = MaterialTheme.typography.labelSmall)
                        Text(formatTime(startTimeMillis), style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedButton(
                    onClick = { showEndTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Wake up", style = MaterialTheme.typography.labelSmall)
                        Text(formatTime(endTimeMillis), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var duration = (endTimeMillis - startTimeMillis).toFloat() / (1000 * 60 * 60)
            if (duration < 0) {
                duration += 24 // Bedtime was yesterday
            }
            Text(
                "Duration: ${String.format("%.1f", duration)} hours",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Quality",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            QualitySlider(
                quality = quality,
                onQualityChange = { quality = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Dream Notes (optional)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = dreamNotes,
                onValueChange = { dreamNotes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Describe any dreams you remember...") },
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Notes (optional)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Any other notes about your sleep...") },
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Sleep Medication Taken?",
                    style = MaterialTheme.typography.bodyLarge
                )
                Checkbox(
                    checked = sleepMedicationTaken,
                    onCheckedChange = { sleepMedicationTaken = it }
                )
            }
        }

        Button(
            onClick = {
                onSave(
                    log?.id,
                    date,
                    startTimeMillis,
                    endTimeMillis,
                    quality,
                    dreamNotes,
                    notes,
                    sleepMedicationTaken,
                    onNavigateBack
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(if (log == null) "Log Sleep" else "Save Changes")
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parsedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton({
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            .plusDays(1)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showDatePicker = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton({ showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        val currentTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeMillis), ZoneId.systemDefault())
        val timePickerState = rememberTimePickerState(currentTime.hour, currentTime.minute)

        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                val selected = LocalTime.of(timePickerState.hour, timePickerState.minute)
                val now = LocalDateTime.now()
                startTimeMillis = now.with(selected).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                showStartTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showEndTimePicker) {
        val currentTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimeMillis), ZoneId.systemDefault())
        val timePickerState = rememberTimePickerState(currentTime.hour, currentTime.minute)

        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                val selected = LocalTime.of(timePickerState.hour, timePickerState.minute)
                val now = LocalDateTime.now()
                endTimeMillis = now.with(selected).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                showEndTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun QualitySlider(
    quality: Float,
    onQualityChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { index ->
                val starValue = (index + 1).toFloat()
                val isFilled = quality >= starValue
                val isHalfFilled = quality >= starValue - 0.5f && quality < starValue

                IconButton(
                    onClick = { 
                        onQualityChange(if (quality == starValue - 0.5f) starValue else starValue - 0.5f) 
                    }
                ) {
                    Icon(
                        imageVector = if (isFilled || isHalfFilled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Star ${index + 1}",
                        tint = when {
                            isFilled -> MaterialTheme.colorScheme.primary
                            isHalfFilled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Slider(
            value = quality,
            onValueChange = { 
                val rounded = ((it * 2).toInt().toFloat() / 2) // Round to nearest 0.5
                onQualityChange(rounded.coerceIn(0.5f, 5f)) 
            },
            valueRange = 0.5f..5f,
            steps = 8, // 0.5 increments = 9 values - 1 = 8 steps
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Poor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Excellent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select time") },
        text = { content() },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Set") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun formatTime(millis: Long): String {
    val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
    return dt.format(DateTimeFormatter.ofPattern("h:mm a"))
}