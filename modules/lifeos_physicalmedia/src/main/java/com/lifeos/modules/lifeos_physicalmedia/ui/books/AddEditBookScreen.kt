package com.lifeos.modules.lifeos_physicalmedia.ui.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lifeos.modules.lifeos_physicalmedia.data.local.BookFormat
import com.lifeos.modules.lifeos_physicalmedia.domain.model.bookSearchQuery
import com.lifeos.modules.lifeos_physicalmedia.domain.model.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookScreen(
    itemId: Long? = null,
    existingTitle: String = "",
    existingAuthor: String = "",
    existingFormat: BookFormat = BookFormat.HARDCOVER,
    existingCoverUrl: String = "",
    existingCoverLocalPath: String = "",
    existingSeriesName: String = "",
    existingSeriesNumber: String = "",
    knownAuthors: List<String> = emptyList(),
    knownSeriesNames: List<String> = emptyList(),
    seriesNumbers: Map<String, List<Float>> = emptyMap(),
    selectedCoverLocalPath: String? = null,
    onSearchCover: (query: String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateBackWithDelete: (() -> Unit)? = null,
    onSave: (title: String, author: String, format: BookFormat, coverUrl: String?, coverLocalPath: String?, seriesName: String?, seriesNumber: Float?) -> Unit
) {
    var title by remember { mutableStateOf(existingTitle) }
    var author by remember { mutableStateOf(existingAuthor) }
    var format by remember { mutableStateOf(existingFormat) }
    var coverUrl by remember { mutableStateOf(existingCoverUrl) }
    var coverLocalPath by remember { mutableStateOf(existingCoverLocalPath) }
    var formatMenuExpanded by remember { mutableStateOf(false) }
    var inSeries by remember { mutableStateOf(existingSeriesName.isNotBlank()) }
    var seriesNameText by remember { mutableStateOf(existingSeriesName) }
    var seriesNumberText by remember { mutableStateOf(existingSeriesNumber) }
    var authorFieldFocused by remember { mutableStateOf(false) }
    var seriesNameFieldFocused by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCoverLocalPath) {
        if (selectedCoverLocalPath != null) {
            coverLocalPath = selectedCoverLocalPath
            coverUrl = ""
        }
    }

    LaunchedEffect(seriesNameText) {
        if (itemId == null && seriesNumberText.isBlank() && seriesNameText.isNotBlank()) {
            val nums = seriesNumbers.entries
                .firstOrNull { it.key.equals(seriesNameText, ignoreCase = true) }?.value
            if (nums != null) {
                val next = (nums.maxOrNull() ?: 0f) + 1f
                seriesNumberText = if (next == next.toLong().toFloat()) next.toInt().toString() else next.toString()
            }
        }
    }

    val authorSuggestions = remember(author, knownAuthors) {
        if (author.isBlank()) emptyList()
        else knownAuthors.filter { it.contains(author, ignoreCase = true) && !it.equals(author, ignoreCase = true) }
    }
    val seriesNameSuggestions = remember(seriesNameText, knownSeriesNames) {
        if (seriesNameText.isBlank()) emptyList()
        else knownSeriesNames.filter { it.contains(seriesNameText, ignoreCase = true) && !it.equals(seriesNameText, ignoreCase = true) }
    }

    val coverModel = coverLocalPath.takeIf { it.isNotBlank() } ?: coverUrl.takeIf { it.isNotBlank() }
    val screenTitle = if (itemId != null) "Edit Book" else "Add Book"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .navigationBarsPadding()
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
                    text = screenTitle,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            if (onNavigateBackWithDelete != null) {
                IconButton(onClick = onNavigateBackWithDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )

        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = authorFieldFocused && authorSuggestions.isNotEmpty(),
            onExpandedChange = {}
        ) {
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Author") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .onFocusChanged { authorFieldFocused = it.isFocused },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            if (authorSuggestions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = authorFieldFocused,
                    onDismissRequest = { authorFieldFocused = false }
                ) {
                    authorSuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = { author = suggestion; authorFieldFocused = false }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = formatMenuExpanded,
            onExpandedChange = { formatMenuExpanded = it }
        ) {
            OutlinedTextField(
                value = format.displayName(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Format") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = formatMenuExpanded,
                onDismissRequest = { formatMenuExpanded = false }
            ) {
                BookFormat.entries.forEach { fmt ->
                    DropdownMenuItem(
                        text = { Text(fmt.displayName()) },
                        onClick = { format = fmt; formatMenuExpanded = false }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Part of a series?", style = MaterialTheme.typography.bodyLarge)
            Checkbox(checked = inSeries, onCheckedChange = {
                inSeries = it
                if (!it) { seriesNameText = ""; seriesNumberText = "" }
            })
        }

        if (inSeries) {
            ExposedDropdownMenuBox(
                expanded = seriesNameFieldFocused && seriesNameSuggestions.isNotEmpty(),
                onExpandedChange = {}
            ) {
                OutlinedTextField(
                    value = seriesNameText,
                    onValueChange = { seriesNameText = it },
                    label = { Text("Series Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .onFocusChanged { seriesNameFieldFocused = it.isFocused },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                if (seriesNameSuggestions.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = seriesNameFieldFocused,
                        onDismissRequest = { seriesNameFieldFocused = false }
                    ) {
                        seriesNameSuggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = { seriesNameText = suggestion; seriesNameFieldFocused = false }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = seriesNumberText,
                onValueChange = { seriesNumberText = it },
                label = { Text("Book # in Series") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(Modifier.height(8.dp))
        }

        if (coverModel != null) {
            AsyncImage(
                model = coverModel,
                contentDescription = "Cover preview",
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick = { onSearchCover(bookSearchQuery(title)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (coverModel == null) "Search for cover" else "Change cover")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val resolvedSeriesName = if (inSeries) seriesNameText.ifBlank { null } else null
                val resolvedSeriesNumber = if (inSeries) seriesNumberText.toFloatOrNull() else null
                onSave(title, author, format, coverUrl.ifBlank { null }, coverLocalPath.ifBlank { null }, resolvedSeriesName, resolvedSeriesNumber)
            },
            enabled = title.isNotBlank() && author.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
