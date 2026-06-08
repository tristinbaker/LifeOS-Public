package com.lifeos.modules.lifeos_financetracker.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_financetracker.data.local.CategoryEntity
import com.lifeos.modules.lifeos_financetracker.data.local.PersonalCardPaymentEntity
import com.lifeos.modules.lifeos_financetracker.data.local.TransactionEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private sealed interface PersonalCardItem {
    val date: String
    data class Charge(val tx: TransactionEntity, val category: CategoryEntity?) : PersonalCardItem {
        override val date get() = tx.date
    }
    data class Payment(val payment: PersonalCardPaymentEntity) : PersonalCardItem {
        override val date get() = payment.date
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalCardScreen(
    balance: Double,
    personalTransactions: List<TransactionEntity>,
    payments: List<PersonalCardPaymentEntity>,
    categories: List<CategoryEntity>,
    onNavigateBack: () -> Unit,
    onAddPayment: (amountCents: Long, note: String, date: String, onComplete: () -> Unit) -> Unit,
    onDeletePayment: (id: Long, onComplete: () -> Unit) -> Unit
) {
    var showPaymentDialog by remember { mutableStateOf(false) }

    val items = remember(personalTransactions, payments) {
        val charges = personalTransactions.map { PersonalCardItem.Charge(it, null) }
        val pays = payments.map { PersonalCardItem.Payment(it) }
        (charges + pays).sortedByDescending { it.date }
    }

    val itemsWithCategories = remember(items, categories) {
        items.map { item ->
            if (item is PersonalCardItem.Charge) item.copy(category = categories.find { it.id == item.tx.categoryId })
            else item
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Card") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                BalanceCard(balance = balance)
            }

            item {
                Button(
                    onClick = { showPaymentDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Record Payment")
                }
            }

            if (itemsWithCategories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No personal charges yet. Tag a transaction as \"Personal\" to track it here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                item {
                    Text("History", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                items(itemsWithCategories, key = { item ->
                    when (item) {
                        is PersonalCardItem.Charge -> "charge_${item.tx.id}"
                        is PersonalCardItem.Payment -> "payment_${item.payment.id}"
                    }
                }) { item ->
                    when (item) {
                        is PersonalCardItem.Charge -> ChargeRow(tx = item.tx, category = item.category)
                        is PersonalCardItem.Payment -> PaymentRow(
                            payment = item.payment,
                            onDelete = { onDeletePayment(item.payment.id) {} }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showPaymentDialog) {
        PaymentDialog(
            onDismiss = { showPaymentDialog = false },
            onSave = { amountCents, note, date ->
                onAddPayment(amountCents, note, date) { showPaymentDialog = false }
            }
        )
    }
}

@Composable
private fun BalanceCard(balance: Double, modifier: Modifier = Modifier) {
    val isOwed = balance > 0
    val balanceColor = if (isOwed) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Balance Owed", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
            Text(
                formatCurrency(balance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = balanceColor
            )
            if (!isOwed) {
                Text("All paid off!", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
            }
        }
    }
}

@Composable
private fun ChargeRow(tx: TransactionEntity, category: CategoryEntity?, modifier: Modifier = Modifier) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tx.note.ifBlank { category?.name ?: "Personal" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text("Charge", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                Text(
                    try { LocalDate.parse(tx.date).format(dateFormatter) } catch (e: Exception) { tx.date },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                "+${formatCurrency(tx.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun PaymentRow(payment: PersonalCardPaymentEntity, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    payment.note.ifBlank { "Payment" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text("Payment", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                Text(
                    try { LocalDate.parse(payment.date).format(dateFormatter) } catch (e: Exception) { payment.date },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                "-${formatCurrency(payment.amountCents / 100.0)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4CAF50)
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun PaymentDialog(
    onDismiss: () -> Unit,
    onSave: (amountCents: Long, note: String, date: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cents = amountText.toDoubleOrNull()?.let { (it * 100).toLong() }
                    if (cents != null && cents > 0) onSave(cents, note, date)
                },
                enabled = amountText.toDoubleOrNull()?.let { it > 0 } == true
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
