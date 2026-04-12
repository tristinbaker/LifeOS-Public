package com.lifeos.modules.lifeos_mealtracker.ui.weight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeos.modules.lifeos_mealtracker.domain.model.WeightEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(
    viewModel: WeightViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var weightToDelete by remember { mutableStateOf<WeightEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weight Tracker") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add weight")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                CurrentWeightCard(
                    currentWeight = uiState.currentWeight,
                    goalWeight = uiState.settings.goalWeight,
                    goalEstimation = uiState.goalEstimation
                )
            }

            if (uiState.weightEntries.size >= 2) {
                item {
                    WeightTrendCard(entries = uiState.weightEntries)
                }
            }

            item {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (uiState.weightEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No weight entries yet. Tap + to add one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.weightEntries.reversed(), key = { it.id }) { entry ->
                    WeightEntryCard(
                        entry = entry,
                        previousEntry = uiState.weightEntries
                            .sortedBy { it.date }
                            .takeWhile { it.date < entry.date }
                            .lastOrNull(),
                        onDelete = { weightToDelete = entry }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddDialog) {
        AddWeightDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { weight ->
                viewModel.addWeight(weight)
                showAddDialog = false
            }
        )
    }

    weightToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { weightToDelete = null },
            title = { Text("Delete Entry") },
            text = { Text("Delete weight entry from ${entry.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWeight(entry)
                        weightToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { weightToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CurrentWeightCard(
    currentWeight: Double?,
    goalWeight: Double?,
    goalEstimation: com.lifeos.modules.lifeos_mealtracker.domain.usecase.GoalEstimation?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current Weight",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentWeight?.let { "%.1f lbs".format(it) } ?: "No data",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            if (goalWeight != null && currentWeight != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val diff = currentWeight - goalWeight
                val direction = if (diff > 0) "to go" else "under"
                Text(
                    text = "%.1f lbs $direction goal".format(kotlin.math.abs(diff)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (goalEstimation != null && goalWeight != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "At this rate, you'll reach your goal by:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = goalEstimation.estimatedDate?.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) ?: "Unable to estimate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun WeightTrendCard(entries: List<WeightEntry>) {
    val sortedEntries = entries.sortedBy { it.date }
    val firstEntry = sortedEntries.first()
    val lastEntry = sortedEntries.last()
    val totalChange = lastEntry.weight - firstEntry.weight
    val daysBetween = ChronoUnit.DAYS.between(firstEntry.date, lastEntry.date)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Start",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "%.1f lbs".format(firstEntry.weight),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "%+.1f lbs".format(totalChange),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (totalChange < 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Period",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$daysBetween days",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WeightEntryCard(
    entry: WeightEntry,
    previousEntry: WeightEntry?,
    onDelete: () -> Unit
) {
    val change = previousEntry?.let { entry.weight - it.weight }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = entry.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (change != null) {
                    Text(
                        text = "%+.1f lbs".format(change),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (change < 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "%.1f lbs".format(entry.weight),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AddWeightDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var weightText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Weight") },
        text = {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Weight (lbs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { weightText.toDoubleOrNull()?.let { onConfirm(it) } }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
