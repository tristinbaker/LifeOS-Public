package com.lifeos.modules.lifeos_financetracker.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_financetracker.data.local.BudgetEntity
import com.lifeos.modules.lifeos_financetracker.data.local.CategoryEntity

private val presetColors = listOf(
    "#FF5722", "#5C6BC0", "#26A69A", "#AB47BC",
    "#EF5350", "#FFA726", "#78909C", "#66BB6A",
    "#42A5F5", "#FFCA28", "#26C6DA", "#BDBDBD"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceSettingsScreen(
    categories: List<CategoryEntity>,
    budgets: List<BudgetEntity>,
    onNavigateBack: () -> Unit,
    onSaveCategory: (id: Long?, name: String, iconName: String, colorHex: String, onComplete: () -> Unit) -> Unit,
    onDeleteCategory: (CategoryEntity, onComplete: () -> Unit) -> Unit,
    onSetBudget: (categoryId: Long, amount: Double, onComplete: () -> Unit) -> Unit,
    onRemoveBudget: (categoryId: Long, onComplete: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val budgetMap = budgets.associateBy { it.categoryId }

    var budgetDialogCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Monthly Budgets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Set spending limits per category",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            items(categories, key = { "budget_${it.id}" }) { cat ->
                val budget = budgetMap[cat.id]
                ListItem(
                    headlineContent = { Text(cat.name) },
                    supportingContent = {
                        Text(
                            budget?.let { formatCurrency(it.monthlyLimitCents / 100.0) + " / month" }
                                ?: "No budget",
                            color = if (budget != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(parseColor(cat.colorHex))
                        )
                    },
                    trailingContent = {
                        TextButton(onClick = { budgetDialogCategory = cat }) {
                            Text(if (budget != null) "Edit" else "Set")
                        }
                    }
                )
                HorizontalDivider()
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Manage your spending categories",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            items(categories, key = { "cat_${it.id}" }) { cat ->
                ListItem(
                    headlineContent = { Text(cat.name) },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(parseColor(cat.colorHex))
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { categoryToDelete = cat }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete category",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                HorizontalDivider()
            }

            item {
                TextButton(
                    onClick = { showAddCategoryDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Category")
                }
            }
        }
    }

    budgetDialogCategory?.let { cat ->
        BudgetDialog(
            category = cat,
            currentBudget = budgetMap[cat.id],
            onDismiss = { budgetDialogCategory = null },
            onConfirm = { amount ->
                onSetBudget(cat.id, amount) { budgetDialogCategory = null }
            },
            onRemove = {
                onRemoveBudget(cat.id) { budgetDialogCategory = null }
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, iconName, colorHex ->
                onSaveCategory(null, name, iconName, colorHex) { showAddCategoryDialog = false }
            }
        )
    }

    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Category") },
            text = { Text("Delete \"${cat.name}\"? Its budget will also be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteCategory(cat) { categoryToDelete = null }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun BudgetDialog(
    category: CategoryEntity,
    currentBudget: BudgetEntity?,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    onRemove: () -> Unit
) {
    var amountText by remember {
        mutableStateOf(currentBudget?.let { "%.2f".format(it.monthlyLimitCents / 100.0) } ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Budget for ${category.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Monthly limit") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                if (currentBudget != null) {
                    TextButton(onClick = onRemove) {
                        Text("Remove budget", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amountText.toDoubleOrNull()?.let { onConfirm(it) }
                },
                enabled = amountText.toDoubleOrNull()?.let { it > 0 } == true
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, iconName: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(presetColors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Color", style = MaterialTheme.typography.labelMedium)
                ColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, "label", selectedColor) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
internal fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = presetColors.chunked(6)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { hex ->
                    val isSelected = hex == selectedColor
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(parseColor(hex))
                            .then(
                                if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                                else Modifier
                            )
                            .clickable { onColorSelected(hex) }
                    )
                }
            }
        }
    }
}
