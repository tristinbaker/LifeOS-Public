package com.lifeos.modules.lifeos_medialogger.ui.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lifeos.modules.lifeos_medialogger.ui.components.RatingSelector
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(
    itemId: Long? = null,
    existingTitle: String = "",
    existingCoverUrl: String = "",
    existingCoverLocalPath: String = "",
    existingAuthor: String = "",
    existingRating: Float? = null,
    existingDate: Long? = null,
    existingNotes: String = "",
    existingPlatform: String = "",
    isMovie: Boolean = false,
    isGame: Boolean = false,
    isSeries: Boolean = false,
    existingIsRewatch: Boolean = false,
    selectedCoverUrl: String? = null,
    selectedTitle: String? = null,
    onSearchCover: ((currentTitle: String) -> Unit)? = null,
    onNavigateBack: () -> Unit,
    onNavigateBackWithDelete: (() -> Unit)? = null,
    onSave: (title: String, coverUrl: String?, rating: Float?, dateCompleted: Long?, notes: String?, platform: String?, author: String?, isRewatch: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(existingTitle) }
    var coverUrl by remember { mutableStateOf(existingCoverUrl) }
    var coverLocalPath by remember { mutableStateOf(existingCoverLocalPath) }
    var author by remember { mutableStateOf(existingAuthor) }
    var platform by remember { mutableStateOf(existingPlatform) }
    var selectedRating by remember { mutableStateOf<Float?>(existingRating) }
    var notes by remember { mutableStateOf(existingNotes) }
    var selectedDate by remember { mutableStateOf(existingDate) }
    var isRewatch by remember { mutableStateOf(existingIsRewatch) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCoverUrl) {
        if (selectedCoverUrl != null) {
            coverUrl = selectedCoverUrl
            coverLocalPath = ""
        }
    }

    LaunchedEffect(selectedTitle) {
        if (selectedTitle != null && title.isBlank()) {
            title = selectedTitle
        }
    }

    val typeLabel = when {
        isMovie -> "Movie"
        isGame -> "Game"
        isSeries -> "Series"
        else -> "Book"
    }

    val coverModel = coverLocalPath.takeIf { it.isNotBlank() } ?: coverUrl.takeIf { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
                Text(
                    text = if (itemId != null) "Edit $typeLabel" else "Add $typeLabel",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (onNavigateBackWithDelete != null) {
                IconButton(onClick = onNavigateBackWithDelete) {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("$typeLabel Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (coverModel != null) {
            AsyncImage(
                model = coverModel,
                contentDescription = "Cover preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (onSearchCover != null) {
            OutlinedButton(
                onClick = { onSearchCover(title) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (coverModel == null) "Search for cover" else "Change cover")
            }
        }

        if (isGame) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = platform,
                onValueChange = { platform = it },
                label = { Text("Platform") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        if (!isMovie && !isGame && !isSeries) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Author") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Rating", style = MaterialTheme.typography.labelLarge)
        RatingSelector(
            selectedRating = selectedRating,
            onRatingSelected = { selectedRating = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = selectedDate?.let {
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it))
            } ?: "",
            onValueChange = {},
            label = { Text("Date Completed") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                TextButton(onClick = { showDatePicker = true }) {
                    Text("Pick")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        if (isMovie || isGame) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(if (isGame) "Replay?" else "Rewatch?", style = MaterialTheme.typography.bodyLarge)
                Checkbox(checked = isRewatch, onCheckedChange = { isRewatch = it })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSave(
                    title,
                    coverUrl.ifBlank { null },
                    selectedRating,
                    selectedDate,
                    notes.ifBlank { null },
                    platform.ifBlank { null },
                    author.ifBlank { null },
                    isRewatch
                )
            },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis?.let { it + (24 * 60 * 60 * 1000) }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
