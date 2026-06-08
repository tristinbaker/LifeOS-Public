package com.lifeos.modules.lifeos_financetracker.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
fun RecurringEditorScreen(
    existing: RecurringTransactionEntity?,
    categories: List<CategoryEntity>,
    accounts: List<AccountWithBalance>,
    onNavigateBack: () -> Unit,
    onSave: (
        id: Long?,
        label: String,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        accountId: Long,
        toAccountId: Long?,
        frequency: RecurringFrequency,
        dayOfMonth: Int?,
        dayOfWeek: Int?,
        nextPostDate: String,
        onComplete: () -> Unit
    ) -> Unit,
    onDelete: (Long, () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var label by remember(existing?.id) { mutableStateOf(existing?.label ?: "") }
    var amountText by remember(existing?.id) { mutableStateOf(existing?.amount?.let { "%.2f".format(it) } ?: "") }
    var selectedType by remember(existing?.id) { mutableStateOf(existing?.type ?: TransactionType.EXPENSE) }
    var selectedAccountId by remember(existing?.id) { mutableStateOf(existing?.accountId?.takeIf { it != 0L }) }
    var selectedToAccountId by remember(existing?.id) { mutableStateOf(existing?.toAccountId) }
    var selectedCategoryId by remember(existing?.id) { mutableStateOf(existing?.categoryId?.takeIf { it != 0L }) }
    var selectedFrequency by remember(existing?.id) { mutableStateOf(existing?.frequency ?: RecurringFrequency.MONTHLY) }
    var dayOfMonthText by remember(existing?.id) { mutableStateOf(existing?.dayOfMonth?.toString() ?: "") }
    var dayOfWeek by remember(existing?.id) { mutableStateOf(existing?.dayOfWeek ?: 1) }
    var startDate by remember(existing?.id) {
        mutableStateOf(
            existing?.nextPostDate?.let { try { LocalDate.parse(it) } catch (e: Exception) { LocalDate.now() } }
                ?: LocalDate.now()
        )
    }

    var accountMenuExpanded by remember { mutableStateOf(false) }
    var toAccountMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var frequencyMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val accountSource = remember { MutableInteractionSource() }
    val toAccountSource = remember { MutableInteractionSource() }
    val categorySource = remember { MutableInteractionSource() }
    val frequencySource = remember { MutableInteractionSource() }

    val accountPressed by accountSource.collectIsPressedAsState()
    val toAccountPressed by toAccountSource.collectIsPressedAsState()
    val categoryPressed by categorySource.collectIsPressedAsState()
    val frequencyPressed by frequencySource.collectIsPressedAsState()

    if (accountPressed) accountMenuExpanded = true
    if (toAccountPressed) toAccountMenuExpanded = true
    if (categoryPressed) categoryMenuExpanded = true
    if (frequencyPressed) frequencyMenuExpanded = true

    val isTransfer = selectedType == TransactionType.TRANSFER
    val needsDayOfMonth = selectedFrequency in setOf(RecurringFrequency.MONTHLY, RecurringFrequency.QUARTERLY, RecurringFrequency.SEMI_ANNUAL, RecurringFrequency.YEARLY)
    val needsDayOfWeek = selectedFrequency == RecurringFrequency.WEEKLY || selectedFrequency == RecurringFrequency.BIWEEKLY

    val isSaveEnabled = label.isNotBlank() &&
            amountText.toDoubleOrNull()?.let { it > 0 } == true &&
            selectedAccountId != null &&
            selectedCategoryId != null &&
            (!isTransfer || selectedToAccountId != null)

    val displayFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text(if (existing == null) "Add Recurring" else "Edit Recurring") },
                actions = {
                    if (existing != null) {
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
                value = label,
                onValueChange = { label = it },
                label = { Text("Label") },
                placeholder = { Text("e.g. Netflix, Mortgage Payment") },
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
                        accounts.filter { it.account.id != selectedAccountId }.forEach { awb ->
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

            // Frequency dropdown
            ExposedDropdownMenuBox(expanded = frequencyMenuExpanded, onExpandedChange = { frequencyMenuExpanded = it }) {
                OutlinedTextField(
                    value = selectedFrequency.displayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frequency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyMenuExpanded) },
                    interactionSource = frequencySource,
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = frequencyMenuExpanded, onDismissRequest = { frequencyMenuExpanded = false }) {
                    RecurringFrequency.entries.forEach { freq ->
                        DropdownMenuItem(
                            text = { Text(freq.displayName()) },
                            onClick = { selectedFrequency = freq; frequencyMenuExpanded = false }
                        )
                    }
                }
            }

            if (needsDayOfMonth) {
                OutlinedTextField(
                    value = dayOfMonthText,
                    onValueChange = { v ->
                        val digits = v.filter { it.isDigit() }
                        val n = digits.toIntOrNull()
                        dayOfMonthText = if (digits.isEmpty()) "" else if (n != null && n <= 31) digits else dayOfMonthText
                    },
                    label = { Text("Day of Month (1–31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (needsDayOfWeek) {
                val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                Text("Day of Week", style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    dayNames.forEachIndexed { index, name ->
                        SegmentedButton(
                            selected = dayOfWeek == index + 1,
                            onClick = { dayOfWeek = index + 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 7)
                        ) { Text(name.take(1)) }
                    }
                }
            }

            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Starts: ${startDate.format(displayFormatter)}")
            }

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: return@Button
                    val categoryId = selectedCategoryId ?: return@Button
                    val accountId = selectedAccountId ?: return@Button
                    onSave(
                        existing?.id, label, amount, selectedType, categoryId, accountId,
                        if (isTransfer) selectedToAccountId else null,
                        selectedFrequency,
                        if (needsDayOfMonth) dayOfMonthText.toIntOrNull()?.coerceIn(1, 31) ?: 1 else null,
                        if (needsDayOfWeek) dayOfWeek else null,
                        startDate.format(isoFormatter)
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
            initialSelectedDateMillis = startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDate = LocalDate.ofEpochDay(millis / 86400000L)
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
            title = { Text("Delete Recurring") },
            text = { Text("Delete \"${existing?.label}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    existing?.let { onDelete(it.id) { onNavigateBack() } }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

private fun RecurringFrequency.displayName(): String = when (this) {
    RecurringFrequency.DAILY -> "Daily"
    RecurringFrequency.WEEKLY -> "Weekly"
    RecurringFrequency.BIWEEKLY -> "Bi-weekly"
    RecurringFrequency.MONTHLY -> "Monthly"
    RecurringFrequency.QUARTERLY -> "Quarterly"
    RecurringFrequency.SEMI_ANNUAL -> "Every 6 Months"
    RecurringFrequency.YEARLY -> "Yearly"
}
