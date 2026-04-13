package com.lifeos.modules.lifeos_journal.ui.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val moodEmojis = listOf("😢", "😕", "😐", "🙂", "😊")
private val moodLabels = listOf("Sad", "Down", "Okay", "Good", "Great")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorScreen(
    entry: com.lifeos.modules.lifeos_journal.data.local.JournalEntryEntity?,
    onNavigateBack: () -> Unit,
    onSave: (
        id: Long?,
        date: String,
        content: String,
        mood: Int,
        onComplete: () -> Unit
    ) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var date by remember(entry) { 
        mutableStateOf(entry?.date ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) 
    }
    var content by remember(entry) { mutableStateOf(entry?.content ?: "") }
    var mood by remember(entry) { mutableStateOf(entry?.mood ?: 3) }

    var showDatePicker by remember { mutableStateOf(false) }

    val parsedDate = try { LocalDate.parse(date) } catch (e: Exception) { LocalDate.now() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = { Text(if (entry == null) "New Entry" else "Edit Entry") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                if (entry != null) {
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
                "How are you feeling?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            MoodSelector(
                mood = mood,
                onMoodChange = { mood = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "What's on your mind?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                placeholder = { Text("Write about your day, thoughts, feelings...") },
                maxLines = 20
            )
        }

        Button(
            onClick = {
                onSave(
                    entry?.id,
                    date,
                    content,
                    mood,
                    onNavigateBack
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = content.isNotBlank()
        ) {
            Text(if (entry == null) "Save Entry" else "Save Changes")
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
}

@Composable
private fun MoodSelector(
    mood: Int,
    onMoodChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        (1..5).forEach { index ->
            val isSelected = mood == index
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onMoodChange(index) }
                    .padding(8.dp)
            ) {
                Text(
                    text = moodEmojis[index - 1],
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = moodLabels[index - 1],
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}