package com.lifeos.modules.lifeos_notes.ui.notes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lifeos.modules.lifeos_notes.data.local.NoteEntity
import com.lifeos.modules.lifeos_notes.ui.components.CheckboxEditor
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    note: NoteEntity?,
    onNavigateBack: () -> Unit,
    onSave: (title: String, content: String, isPinned: Boolean, notificationTime: Long?) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var title by remember(note) { mutableStateOf(note?.title ?: "") }
    var content by remember(note) { mutableStateOf(note?.content ?: "") }
    var isPinned by remember(note) { mutableStateOf(note?.isPinned ?: false) }
    var notificationTime by remember(note) { mutableStateOf(note?.notificationTime) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateTime by remember { mutableStateOf<LocalDateTime?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showDatePicker = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = { Text(if (note == null) "New Note" else "Edit Note") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton({ isPinned = !isPinned }) {
                    Icon(Icons.Filled.PushPin, "Pin", tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                }
                if (note != null) {
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
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title") },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            CheckboxEditor(
                content = content,
                onContentChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Text("Reminder", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (notificationTime != null) {
                    val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(notificationTime!!), ZoneId.systemDefault())
                    Text(dt.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")), color = MaterialTheme.colorScheme.primary)
                } else {
                    Text("No reminder", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }

            Row {
                if (notificationTime != null) {
                    TextButton({ notificationTime = null }) { Text("Clear") }
                }
                TextButton({
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            showDatePicker = true
                        } else {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        showDatePicker = true
                    }
                }) {
                    Icon(Icons.Default.Notifications, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (notificationTime == null) "Add" else "Change")
                }
            }
        }

        Button(
            onClick = { onSave(title, content, isPinned, notificationTime); onNavigateBack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Text("Save Note")
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton({
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).withHour(9).withMinute(0)
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton({ showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(datePickerState)
        }
    }

    if (showTimePicker && selectedDateTime != null) {
        val timePickerState = rememberTimePickerState(selectedDateTime!!.hour, selectedDateTime!!.minute)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(timePickerState) },
            confirmButton = {
                TextButton({
                    val dt = selectedDateTime!!.withHour(timePickerState.hour).withMinute(timePickerState.minute)
                    notificationTime = dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    showTimePicker = false
                    selectedDateTime = null
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton({ showTimePicker = false; selectedDateTime = null }) { Text("Cancel") }
            }
        )
    }
}
