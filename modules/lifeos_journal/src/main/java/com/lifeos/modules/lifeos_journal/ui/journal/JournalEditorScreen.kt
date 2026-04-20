package com.lifeos.modules.lifeos_journal.ui.journal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.lifeos.modules.lifeos_journal.data.local.JournalImageEntity
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val moodEmojis = listOf("😢", "😕", "😐", "🙂", "😊")
private val moodLabels = listOf("Sad", "Down", "Okay", "Good", "Great")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun JournalEditorScreen(
    entry: com.lifeos.modules.lifeos_journal.data.local.JournalEntryEntity?,
    images: List<JournalImageEntity> = emptyList(),
    onNavigateBack: () -> Unit,
    onSave: (
        id: Long?,
        date: String,
        content: String,
        mood: Int,
        newImageUris: List<Uri>,
        removedImageIds: Set<Long>,
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

    var newImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var removedImageIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    val displayedExistingImages = remember(images, removedImageIds) {
        images.filter { it.id !in removedImageIds }
    }

    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    val allViewerItems: List<Any> = remember(displayedExistingImages, newImageUris) {
        displayedExistingImages.map { File(it.localPath) } + newImageUris
    }

    // Long-press removal state
    var pendingRemoveImage by remember { mutableStateOf<JournalImageEntity?>(null) }
    var pendingRemoveUri by remember { mutableStateOf<Uri?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    val parsedDate = try { LocalDate.parse(date) } catch (e: Exception) { LocalDate.now() }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        newImageUris = newImageUris + uris
    }

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

            MoodSelector(mood = mood, onMoodChange = { mood = it })

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
                maxLines = 20,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Memories",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(
                    onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Photos")
                }
            }

            if (displayedExistingImages.isNotEmpty() || newImageUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(displayedExistingImages, key = { "existing_${it.id}" }) { image ->
                        AsyncImage(
                            model = File(image.localPath),
                            contentDescription = "Memory",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .combinedClickable(
                                    onClick = { viewerIndex = displayedExistingImages.indexOf(image) },
                                    onLongClick = { pendingRemoveImage = image }
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }
                    items(newImageUris, key = { "new_$it" }) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "New memory",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .combinedClickable(
                                    onClick = { viewerIndex = displayedExistingImages.size + newImageUris.indexOf(uri) },
                                    onLongClick = { pendingRemoveUri = uri }
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                onSave(entry?.id, date, content, mood, newImageUris, removedImageIds, onNavigateBack)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = content.isNotBlank()
        ) {
            Text(if (entry == null) "Save Entry" else "Save Changes")
        }
    }

    // Full-screen swipeable image viewer
    viewerIndex?.let { startIndex ->
        Dialog(
            onDismissRequest = { viewerIndex = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val pagerState = rememberPagerState(initialPage = startIndex) { allViewerItems.size }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { viewerIndex = null }
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = allViewerItems[page],
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                if (allViewerItems.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(allViewerItems.size) { i ->
                            Box(
                                Modifier
                                    .size(if (i == pagerState.currentPage) 8.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i == pagerState.currentPage) Color.White
                                        else Color.White.copy(alpha = 0.4f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }

    // Long-press remove confirmation for existing image
    if (pendingRemoveImage != null) {
        AlertDialog(
            onDismissRequest = { pendingRemoveImage = null },
            text = { Text("Remove this photo from Memories?") },
            confirmButton = {
                TextButton(onClick = {
                    pendingRemoveImage?.let { removedImageIds = removedImageIds + it.id }
                    pendingRemoveImage = null
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoveImage = null }) { Text("Cancel") }
            }
        )
    }

    // Long-press remove confirmation for newly added image
    if (pendingRemoveUri != null) {
        AlertDialog(
            onDismissRequest = { pendingRemoveUri = null },
            text = { Text("Remove this photo from Memories?") },
            confirmButton = {
                TextButton(onClick = {
                    pendingRemoveUri?.let { newImageUris = newImageUris - it }
                    pendingRemoveUri = null
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoveUri = null }) { Text("Cancel") }
            }
        )
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
