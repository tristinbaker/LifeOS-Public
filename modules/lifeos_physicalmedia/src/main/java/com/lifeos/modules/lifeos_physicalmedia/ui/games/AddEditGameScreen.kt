package com.lifeos.modules.lifeos_physicalmedia.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lifeos.modules.lifeos_physicalmedia.data.local.GameSystem
import com.lifeos.modules.lifeos_physicalmedia.domain.model.displayName
import com.lifeos.modules.lifeos_physicalmedia.domain.model.gameSearchQuery
import com.lifeos.modules.lifeos_physicalmedia.domain.model.gameSystemGroups

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGameScreen(
    itemId: Long? = null,
    existingTitle: String = "",
    existingSystem: GameSystem = GameSystem.SWITCH,
    existingCoverUrl: String = "",
    existingCoverLocalPath: String = "",
    existingIsCollection: Boolean = false,
    existingCollectionItems: List<String> = emptyList(),
    selectedCoverLocalPath: String? = null,
    onSearchCover: (query: String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateBackWithDelete: (() -> Unit)? = null,
    onSave: (title: String, system: GameSystem, coverUrl: String?, coverLocalPath: String?, isCollection: Boolean, collectionItems: List<String>) -> Unit
) {
    var title by remember { mutableStateOf(existingTitle) }
    var system by remember { mutableStateOf(existingSystem) }
    var coverUrl by remember { mutableStateOf(existingCoverUrl) }
    var coverLocalPath by remember { mutableStateOf(existingCoverLocalPath) }
    var isCollection by remember { mutableStateOf(existingIsCollection) }
    val collectionTitles: SnapshotStateList<String> = remember { mutableStateListOf(*existingCollectionItems.toTypedArray()) }
    var systemMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCoverLocalPath) {
        if (selectedCoverLocalPath != null) {
            coverLocalPath = selectedCoverLocalPath
            coverUrl = ""
        }
    }

    val coverModel = coverLocalPath.takeIf { it.isNotBlank() } ?: coverUrl.takeIf { it.isNotBlank() }
    val screenTitle = if (itemId != null) "Edit Game" else "Add Game"

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
            expanded = systemMenuExpanded,
            onExpandedChange = { systemMenuExpanded = it }
        ) {
            OutlinedTextField(
                value = system.displayName(),
                onValueChange = {},
                readOnly = true,
                label = { Text("System") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = systemMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = systemMenuExpanded,
                onDismissRequest = { systemMenuExpanded = false }
            ) {
                gameSystemGroups().forEach { (groupName, systems) ->
                    Box(modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 2.dp)) {
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    systems.forEach { sys ->
                        DropdownMenuItem(
                            text = { Text(sys.displayName()) },
                            onClick = { system = sys; systemMenuExpanded = false },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Collection?", style = MaterialTheme.typography.bodyLarge)
            Checkbox(checked = isCollection, onCheckedChange = { isCollection = it })
        }

        if (isCollection) {
            Text("Collection Contents", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
            collectionTitles.forEachIndexed { index, titleText ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { collectionTitles[index] = it },
                        label = { Text("Title ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    IconButton(onClick = { collectionTitles.removeAt(index) }) {
                        Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            TextButton(
                onClick = { collectionTitles.add("") },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Title")
            }
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
            onClick = { onSearchCover(gameSearchQuery(title, system)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (coverModel == null) "Search for cover" else "Change cover")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onSave(title, system, coverUrl.ifBlank { null }, coverLocalPath.ifBlank { null }, isCollection, collectionTitles.toList()) },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
