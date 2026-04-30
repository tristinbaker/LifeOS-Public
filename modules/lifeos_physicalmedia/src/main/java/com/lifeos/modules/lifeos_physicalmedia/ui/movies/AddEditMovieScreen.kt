package com.lifeos.modules.lifeos_physicalmedia.ui.movies

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lifeos.modules.lifeos_physicalmedia.data.local.MovieFormat
import com.lifeos.modules.lifeos_physicalmedia.domain.model.displayName
import com.lifeos.modules.lifeos_physicalmedia.domain.model.movieSearchQuery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMovieScreen(
    itemId: Long? = null,
    existingTitle: String = "",
    existingFormat: MovieFormat = MovieFormat.BLU_RAY,
    existingLimitedEdition: Boolean = false,
    existingSteelbook: Boolean = false,
    existingSlipcover: Boolean = false,
    existingBoutiqueLabel: String = "",
    existingCatalogNumber: String = "",
    existingCoverUrl: String = "",
    existingCoverLocalPath: String = "",
    knownBoutiqueLabels: List<String> = emptyList(),
    selectedCoverLocalPath: String? = null,
    onSearchCover: (query: String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateBackWithDelete: (() -> Unit)? = null,
    onSave: (title: String, format: MovieFormat, limitedEdition: Boolean, steelbook: Boolean, slipcover: Boolean, boutiqueLabel: String?, catalogNumber: String?, coverUrl: String?, coverLocalPath: String?) -> Unit
) {
    var title by remember { mutableStateOf(existingTitle) }
    var format by remember { mutableStateOf(existingFormat) }
    var limitedEdition by remember { mutableStateOf(existingLimitedEdition) }
    var steelbook by remember { mutableStateOf(existingSteelbook) }
    var slipcover by remember { mutableStateOf(existingSlipcover) }
    var inBoutiqueLabel by remember { mutableStateOf(existingBoutiqueLabel.isNotBlank()) }
    var boutiqueLabelText by remember { mutableStateOf(existingBoutiqueLabel) }
    var catalogNumberText by remember { mutableStateOf(existingCatalogNumber) }
    var coverUrl by remember { mutableStateOf(existingCoverUrl) }
    var coverLocalPath by remember { mutableStateOf(existingCoverLocalPath) }
    var formatMenuExpanded by remember { mutableStateOf(false) }
    var boutiqueLabelFieldFocused by remember { mutableStateOf(false) }

    val boutiqueLabelSuggestions = remember(boutiqueLabelText, knownBoutiqueLabels) {
        if (boutiqueLabelText.isBlank()) emptyList()
        else knownBoutiqueLabels.filter { it.contains(boutiqueLabelText, ignoreCase = true) && !it.equals(boutiqueLabelText, ignoreCase = true) }
    }

    LaunchedEffect(selectedCoverLocalPath) {
        if (selectedCoverLocalPath != null) {
            coverLocalPath = selectedCoverLocalPath
            coverUrl = ""
        }
    }

    val coverModel = coverLocalPath.takeIf { it.isNotBlank() } ?: coverUrl.takeIf { it.isNotBlank() }
    val screenTitle = if (itemId != null) "Edit Movie" else "Add Movie"

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

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Limited Edition", style = MaterialTheme.typography.bodyLarge)
            Checkbox(checked = limitedEdition, onCheckedChange = { limitedEdition = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Steelbook", style = MaterialTheme.typography.bodyLarge)
            Checkbox(checked = steelbook, onCheckedChange = { steelbook = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Slipcover", style = MaterialTheme.typography.bodyLarge)
            Checkbox(checked = slipcover, onCheckedChange = { slipcover = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Boutique label?", style = MaterialTheme.typography.bodyLarge)
            Checkbox(checked = inBoutiqueLabel, onCheckedChange = {
                inBoutiqueLabel = it
                if (!it) { boutiqueLabelText = ""; catalogNumberText = "" }
            })
        }

        if (inBoutiqueLabel) {
            ExposedDropdownMenuBox(
                expanded = boutiqueLabelFieldFocused && boutiqueLabelSuggestions.isNotEmpty(),
                onExpandedChange = {}
            ) {
                OutlinedTextField(
                    value = boutiqueLabelText,
                    onValueChange = { boutiqueLabelText = it },
                    label = { Text("Boutique Label") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .onFocusChanged { boutiqueLabelFieldFocused = it.isFocused },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                if (boutiqueLabelSuggestions.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = boutiqueLabelFieldFocused,
                        onDismissRequest = { boutiqueLabelFieldFocused = false }
                    ) {
                        boutiqueLabelSuggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = { boutiqueLabelText = suggestion; boutiqueLabelFieldFocused = false }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = catalogNumberText,
                onValueChange = { catalogNumberText = it },
                label = { Text("Catalog Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(4.dp))

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
            onClick = { onSearchCover(movieSearchQuery(title, format)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (coverModel == null) "Search for cover" else "Change cover")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val resolvedLabel = if (inBoutiqueLabel) boutiqueLabelText.ifBlank { null } else null
                val resolvedCatalog = if (inBoutiqueLabel) catalogNumberText.ifBlank { null } else null
                onSave(title, format, limitedEdition, steelbook, slipcover, resolvedLabel, resolvedCatalog, coverUrl.ifBlank { null }, coverLocalPath.ifBlank { null })
            },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
