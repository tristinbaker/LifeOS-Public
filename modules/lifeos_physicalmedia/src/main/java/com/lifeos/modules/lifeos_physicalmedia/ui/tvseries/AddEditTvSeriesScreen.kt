package com.lifeos.modules.lifeos_physicalmedia.ui.tvseries

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
import com.lifeos.modules.lifeos_physicalmedia.data.local.MovieFormat
import com.lifeos.modules.lifeos_physicalmedia.domain.model.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTvSeriesScreen(
    itemId: Long? = null,
    existingTitle: String = "",
    existingFormat: MovieFormat = MovieFormat.BLU_RAY,
    existingCompleteSeries: Boolean = false,
    existingSeriesName: String = "",
    existingSeriesNumber: String = "",
    knownSeriesNames: List<String> = emptyList(),
    seriesNumbers: Map<String, List<Float>> = emptyMap(),
    selectedCoverLocalPath: String? = null,
    onSearchCover: (query: String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateBackWithDelete: (() -> Unit)? = null,
    onSave: (title: String, format: MovieFormat, completeSeries: Boolean, seriesName: String?, seriesNumber: Float?, coverUrl: String?, coverLocalPath: String?) -> Unit
) {
    var title by remember { mutableStateOf(existingTitle) }
    var format by remember { mutableStateOf(existingFormat) }
    var completeSeries by remember { mutableStateOf(existingCompleteSeries) }
    var inSeries by remember { mutableStateOf(existingSeriesName.isNotBlank()) }
    var seriesNameText by remember { mutableStateOf(existingSeriesName) }
    var seriesNumberText by remember { mutableStateOf(existingSeriesNumber) }
    var coverUrl by remember { mutableStateOf("") }
    var coverLocalPath by remember { mutableStateOf("") }
    var formatMenuExpanded by remember { mutableStateOf(false) }
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

    val seriesNameSuggestions = remember(seriesNameText, knownSeriesNames) {
        if (seriesNameText.isBlank()) emptyList()
        else knownSeriesNames.filter {
            it.contains(seriesNameText, ignoreCase = true) && !it.equals(seriesNameText, ignoreCase = true)
        }
    }

    val coverModel = coverLocalPath.takeIf { it.isNotBlank() } ?: coverUrl.takeIf { it.isNotBlank() }
    val screenTitle = if (itemId != null) "Edit TV Series" else "Add TV Series"

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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Format dropdown
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
                MovieFormat.entries.forEach { fmt ->
                    DropdownMenuItem(
                        text = { Text(fmt.displayName()) },
                        onClick = { format = fmt; formatMenuExpanded = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Complete series checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = completeSeries, onCheckedChange = { completeSeries = it })
            Text("Complete series?", style = MaterialTheme.typography.bodyLarge)
        }

        // Part of a series checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = inSeries,
                onCheckedChange = {
                    inSeries = it
                    if (!it) { seriesNameText = ""; seriesNumberText = "" }
                }
            )
            Text("Part of a series?", style = MaterialTheme.typography.bodyLarge)
        }

        if (inSeries) {
            Spacer(modifier = Modifier.height(8.dp))
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
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = seriesNumberText,
                onValueChange = { seriesNumberText = it },
                label = { Text("Series #") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cover image
        if (coverModel != null) {
            AsyncImage(
                model = coverModel,
                contentDescription = "Cover",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick = { onSearchCover("$title TV series cover") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Search, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Search for cover")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val resolvedSeriesName = if (inSeries) seriesNameText.ifBlank { null } else null
                val resolvedSeriesNumber = if (inSeries) seriesNumberText.toFloatOrNull() else null
                onSave(
                    title.trim(),
                    format,
                    completeSeries,
                    resolvedSeriesName,
                    resolvedSeriesNumber,
                    coverUrl.ifBlank { null },
                    coverLocalPath.ifBlank { null }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank()
        ) {
            Text(if (itemId != null) "Save Changes" else "Add TV Series")
        }
    }
}
