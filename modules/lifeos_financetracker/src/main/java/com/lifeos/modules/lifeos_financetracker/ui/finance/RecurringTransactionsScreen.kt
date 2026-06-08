package com.lifeos.modules.lifeos_financetracker.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_financetracker.data.local.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionsScreen(
    recurring: List<RecurringTransactionEntity>,
    categories: List<CategoryEntity>,
    accounts: List<com.lifeos.modules.lifeos_financetracker.data.repository.AccountWithBalance>,
    onNavigateBack: () -> Unit,
    onAddRecurring: () -> Unit,
    onEditRecurring: (Long) -> Unit,
    onDeleteRecurring: (Long, () -> Unit) -> Unit,
    onToggleRecurring: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var itemToDelete by remember { mutableStateOf<RecurringTransactionEntity?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Recurring Transactions") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecurring) {
                Icon(Icons.Default.Add, contentDescription = "Add recurring")
            }
        }
    ) { paddingValues ->
        if (recurring.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No recurring transactions", style = MaterialTheme.typography.titleMedium)
                    Text("Tap + to add one", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recurring, key = { it.id }) { item ->
                    val category = categories.find { it.id == item.categoryId }
                    val account = accounts.find { it.account.id == item.accountId }?.account
                    RecurringRow(
                        item = item,
                        category = category,
                        accountName = account?.name ?: "",
                        onEdit = { onEditRecurring(item.id) },
                        onDelete = { itemToDelete = item },
                        onToggle = { onToggleRecurring(item.id, it) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Recurring") },
            text = { Text("Delete \"${item.label}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteRecurring(item.id) { itemToDelete = null }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RecurringRow(
    item: RecurringTransactionEntity,
    category: CategoryEntity?,
    accountName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    buildString {
                        append(formatCurrency(item.amount))
                        append(" · ")
                        append(frequencyDescription(item))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (accountName.isNotBlank()) {
                    Text(accountName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                Text(
                    "Next: ${item.nextPostDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            Switch(checked = item.isActive, onCheckedChange = onToggle)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun frequencyDescription(item: RecurringTransactionEntity): String = when (item.frequency) {
    RecurringFrequency.DAILY -> "Daily"
    RecurringFrequency.WEEKLY -> item.dayOfWeek?.let { "Weekly on ${dayName(it)}" } ?: "Weekly"
    RecurringFrequency.BIWEEKLY -> item.dayOfWeek?.let { "Every 2 weeks on ${dayName(it)}" } ?: "Bi-weekly"
    RecurringFrequency.MONTHLY -> item.dayOfMonth?.let { "Monthly on the ${ordinal(it)}" } ?: "Monthly"
    RecurringFrequency.QUARTERLY -> item.dayOfMonth?.let { "Quarterly on the ${ordinal(it)}" } ?: "Quarterly"
    RecurringFrequency.SEMI_ANNUAL -> item.dayOfMonth?.let { "Every 6 months on the ${ordinal(it)}" } ?: "Semi-annual"
    RecurringFrequency.YEARLY -> item.dayOfMonth?.let { "Yearly on the ${ordinal(it)}" } ?: "Yearly"
}

private fun dayName(dow: Int): String = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").getOrElse(dow - 1) { "?" }

private fun ordinal(n: Int): String = when {
    n in 11..13 -> "${n}th"
    n % 10 == 1 -> "${n}st"
    n % 10 == 2 -> "${n}nd"
    n % 10 == 3 -> "${n}rd"
    else -> "${n}th"
}
