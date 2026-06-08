package com.lifeos.modules.lifeos_financetracker.ui.finance

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_financetracker.data.local.*
import com.lifeos.modules.lifeos_financetracker.data.repository.AccountWithBalance
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditorScreen(
    transaction: TransactionEntity?,
    categories: List<CategoryEntity>,
    accounts: List<AccountWithBalance>,
    onNavigateBack: () -> Unit,
    onSave: (id: Long?, amount: Double, type: TransactionType, categoryId: Long, accountId: Long, toAccountId: Long?, date: String, note: String, onComplete: () -> Unit) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var amountText by remember(transaction?.id) { mutableStateOf(transaction?.amount?.let { "%.2f".format(it) } ?: "") }
    var selectedType by remember(transaction?.id) { mutableStateOf(transaction?.type ?: TransactionType.EXPENSE) }
    var selectedAccountId by remember(transaction?.id) { mutableStateOf(transaction?.accountId?.takeIf { it != 0L }) }
    var selectedToAccountId by remember(transaction?.id) { mutableStateOf(transaction?.toAccountId) }
    var selectedCategoryId by remember(transaction?.id) { mutableStateOf(transaction?.categoryId?.takeIf { it != 0L }) }
    var selectedDate by remember(transaction?.id) {
        mutableStateOf(transaction?.date?.let { try { LocalDate.parse(it) } catch (e: Exception) { LocalDate.now() } } ?: LocalDate.now())
    }
    var noteText by remember(transaction?.id) { mutableStateOf(transaction?.note ?: "") }

    var accountMenuExpanded by remember { mutableStateOf(false) }
    var toAccountMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val accountSource = remember { MutableInteractionSource() }
    val toAccountSource = remember { MutableInteractionSource() }
    val categorySource = remember { MutableInteractionSource() }

    val accountPressed by accountSource.collectIsPressedAsState()
    val toAccountPressed by toAccountSource.collectIsPressedAsState()
    val categoryPressed by categorySource.collectIsPressedAsState()

    if (accountPressed) accountMenuExpanded = true
    if (toAccountPressed) toAccountMenuExpanded = true
    if (categoryPressed) categoryMenuExpanded = true

    val isTransfer = selectedType == TransactionType.TRANSFER
    val isSaveEnabled = amountText.toDoubleOrNull()?.let { it > 0 } == true &&
            selectedAccountId != null &&
            selectedCategoryId != null &&
            noteText.isNotBlank() &&
            (!isTransfer || selectedToAccountId != null)

    val displayFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    val toAccountOptions = accounts.filter { it.account.id != selectedAccountId }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text(if (transaction == null) "Add Transaction" else "Edit Transaction") },
                actions = {
                    if (transaction != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount") },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER).forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                    ) {
                        Text(when (type) {
                            TransactionType.EXPENSE -> "Expense"
                            TransactionType.INCOME -> "Income"
                            TransactionType.TRANSFER -> "Payment"
                        })
                    }
                }
            }

            // Account dropdown
            ExposedDropdownMenuBox(expanded = accountMenuExpanded, onExpandedChange = { accountMenuExpanded = it }) {
                OutlinedTextField(
                    value = accounts.find { it.account.id == selectedAccountId }?.account?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isTransfer) "From Account" else "Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountMenuExpanded) },
                    interactionSource = accountSource,
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = accountMenuExpanded, onDismissRequest = { accountMenuExpanded = false }) {
                    accounts.forEach { awb ->
                        DropdownMenuItem(
                            text = { Text("${awb.account.name} (${awb.account.type.displayName()})") },
                            onClick = { selectedAccountId = awb.account.id; accountMenuExpanded = false }
                        )
                    }
                }
            }

            // To Account dropdown (only for Transfer/Payment)
            if (isTransfer) {
                ExposedDropdownMenuBox(expanded = toAccountMenuExpanded, onExpandedChange = { toAccountMenuExpanded = it }) {
                    OutlinedTextField(
                        value = accounts.find { it.account.id == selectedToAccountId }?.account?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toAccountMenuExpanded) },
                        interactionSource = toAccountSource,
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = toAccountMenuExpanded, onDismissRequest = { toAccountMenuExpanded = false }) {
                        toAccountOptions.forEach { awb ->
                            DropdownMenuItem(
                                text = { Text("${awb.account.name} (${awb.account.type.displayName()})") },
                                onClick = { selectedToAccountId = awb.account.id; toAccountMenuExpanded = false }
                            )
                        }
                    }
                }
            }

            // Category dropdown
            ExposedDropdownMenuBox(expanded = categoryMenuExpanded, onExpandedChange = { categoryMenuExpanded = it }) {
                OutlinedTextField(
                    value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    interactionSource = categorySource,
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = { selectedCategoryId = cat.id; categoryMenuExpanded = false }
                        )
                    }
                }
            }

            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedDate.format(displayFormatter))
            }

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Vendor") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: return@Button
                    val categoryId = selectedCategoryId ?: return@Button
                    val accountId = selectedAccountId ?: return@Button
                    onSave(
                        transaction?.id, amount, selectedType, categoryId, accountId,
                        if (isTransfer) selectedToAccountId else null,
                        selectedDate.format(isoFormatter), noteText
                    ) { onNavigateBack() }
                },
                enabled = isSaveEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = LocalDate.ofEpochDay(millis / 86400000L)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Transaction") },
            text = { Text("Delete this transaction?") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}
