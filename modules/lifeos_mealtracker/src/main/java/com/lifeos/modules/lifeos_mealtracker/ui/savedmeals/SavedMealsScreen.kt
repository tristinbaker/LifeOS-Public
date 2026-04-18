package com.lifeos.modules.lifeos_mealtracker.ui.savedmeals

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealType
import com.lifeos.modules.lifeos_mealtracker.domain.model.SavedMeal
import com.lifeos.modules.lifeos_mealtracker.domain.model.StoredItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedMealsScreen(
    onMealSelected: (SavedMeal) -> Unit,
    onStoredItemSelected: (StoredItem, Double) -> Unit,
    onAddStoredItem: () -> Unit,
    onEditStoredItem: (Long) -> Unit,
    viewModel: SavedMealsViewModel = hiltViewModel()
) {
    val savedMeals by viewModel.savedMeals.collectAsState()
    val storedItems by viewModel.storedItems.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var mealToDelete by remember { mutableStateOf<SavedMeal?>(null) }
    var storedItemToDelete by remember { mutableStateOf<StoredItem?>(null) }
    var storedItemToLog by remember { mutableStateOf<StoredItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Meals") }
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(onClick = onAddStoredItem) {
                    Icon(Icons.Default.Add, contentDescription = "Add Stored Item")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Saved Meals") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Stored Items") }
                )
            }

            when (selectedTab) {
                0 -> SavedMealsTab(
                    savedMeals = savedMeals,
                    onMealSelected = onMealSelected,
                    onDeleteMeal = { mealToDelete = it }
                )
                1 -> StoredItemsTab(
                    storedItems = storedItems,
                    onStoredItemSelected = { storedItemToLog = it },
                    onEditStoredItem = { onEditStoredItem(it.id) },
                    onDeleteStoredItem = { storedItemToDelete = it }
                )
            }
        }
    }

    mealToDelete?.let { meal ->
        AlertDialog(
            onDismissRequest = { mealToDelete = null },
            title = { Text("Delete Saved Meal") },
            text = { Text("Are you sure you want to delete '${meal.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSavedMeal(meal)
                        mealToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mealToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    storedItemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { storedItemToDelete = null },
            title = { Text("Delete Stored Item") },
            text = { Text("Are you sure you want to delete '${item.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteStoredItem(item)
                        storedItemToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { storedItemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    storedItemToLog?.let { item ->
        StoredItemQuantityDialog(
            item = item,
            onDismiss = { storedItemToLog = null },
            onConfirm = { quantity ->
                onStoredItemSelected(item, quantity)
                storedItemToLog = null
            }
        )
    }
}

@Composable
private fun SavedMealsTab(
    savedMeals: List<SavedMeal>,
    onMealSelected: (SavedMeal) -> Unit,
    onDeleteMeal: (SavedMeal) -> Unit
) {
    if (savedMeals.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No saved meals yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Save meals as favorites when adding them",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(savedMeals, key = { it.id }) { meal ->
                SavedMealCard(
                    meal = meal,
                    onClick = { onMealSelected(meal) },
                    onDelete = { onDeleteMeal(meal) }
                )
            }
        }
    }
}

@Composable
private fun StoredItemsTab(
    storedItems: List<StoredItem>,
    onStoredItemSelected: (StoredItem) -> Unit,
    onEditStoredItem: (StoredItem) -> Unit,
    onDeleteStoredItem: (StoredItem) -> Unit
) {
    if (storedItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No stored items yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap + to add reusable items like eggs, chicken, etc.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(storedItems, key = { it.id }) { item ->
                StoredItemCard(
                    item = item,
                    onClick = { onStoredItemSelected(item) },
                    onLongClick = { onEditStoredItem(item) },
                    onDelete = { onDeleteStoredItem(item) }
                )
            }
        }
    }
}

@Composable
private fun StoredItemQuantityDialog(
    item: StoredItem,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var quantity by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name) },
        text = {
            Column {
                Text(
                    text = "Per unit: ${item.caloriesPerUnit} cal | P: ${item.proteinPerUnit}g | C: ${item.carbsPerUnit}g | F: ${item.fatPerUnit}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                val qty = quantity.toDoubleOrNull() ?: 0.0
                if (qty > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total: ${(item.caloriesPerUnit * qty).toInt()} cal | P: ${(item.proteinPerUnit * qty).toInt()}g | C: ${(item.carbsPerUnit * qty).toInt()}g | F: ${(item.fatPerUnit * qty).toInt()}g",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    quantity.toDoubleOrNull()?.let { onConfirm(it) }
                },
                enabled = quantity.toDoubleOrNull()?.let { it > 0 } == true
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

@Composable
fun SavedMealCard(
    meal: SavedMeal,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (meal.mealType) {
                    MealType.BREAKFAST -> "Breakfast"
                    MealType.LUNCH -> "Lunch"
                    MealType.DINNER -> "Dinner"
                    MealType.SNACK -> "Snack"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${meal.calories} cal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "P: ${meal.protein}g | C: ${meal.carbs}g | F: ${meal.fat}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoredItemCard(
    item: StoredItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap to log",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${item.caloriesPerUnit} cal/unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "P: ${item.proteinPerUnit}g | C: ${item.carbsPerUnit}g | F: ${item.fatPerUnit}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
