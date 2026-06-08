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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinkingFundEditorScreen(
    existing: SinkingFundEntity?,
    accounts: List<AccountWithBalance>,
    categories: List<CategoryEntity>,
    onNavigateBack: () -> Unit,
    onSave: (
        id: Long?,
        name: String,
        targetDollars: Double,
        initialDollars: Double,
        targetDate: String,
        colorHex: String,
        accountId: Long?,
        categoryId: Long?,
        contributionDayOfMonth: Int?,
        onComplete: () -> Unit
    ) -> Unit,
    onDelete: ((Long, () -> Unit) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    var name by remember(existing?.id) { mutableStateOf(existing?.name ?: "") }
    var targetText by remember(existing?.id) { mutableStateOf(existing?.let { "%.2f".format(it.targetAmountCents / 100.0) } ?: "") }
    var initialText by remember(existing?.id) { mutableStateOf(existing?.let { "%.2f".format(it.initialBalanceCents / 100.0) } ?: "0.00") }
    var selectedColor by remember(existing?.id) { mutableStateOf(existing?.colorHex ?: "#66BB6A") }
    var selectedAccountId by remember(existing?.id) { mutableStateOf(existing?.accountId) }
    var selectedCategoryId by remember(existing?.id) { mutableStateOf(existing?.categoryId) }
    var contributionDayText by remember(existing?.id) {
        mutableStateOf(existing?.contributionDayOfMonth?.toString() ?: "")
    }
    var targetDate by remember(existing?.id) {
        mutableStateOf(
            existing?.targetDate?.let { try { LocalDate.parse(it) } catch (e: Exception) { LocalDate.now().plusMonths(6) } }
                ?: LocalDate.now().plusMonths(6)
        )
    }

    var accountMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val accountSource = remember { MutableInteractionSource() }
    val categorySource = remember { MutableInteractionSource() }
    val accountPressed by accountSource.collectIsPressedAsState()
    val categoryPressed by categorySource.collectIsPressedAsState()
    if (accountPressed) accountMenuExpanded = true
    if (categoryPressed) categoryMenuExpanded = true

    val displayFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    // Live-computed monthly contribution
    val computedMonthly = remember(targetText, initialText, targetDate) {
        val target = (targetText.toDoubleOrNull() ?: 0.0) * 100
        val initial = (initialText.toDoubleOrNull() ?: 0.0) * 100
        val remaining = (target - initial).coerceAtLeast(0.0)
        val targetYearMonth = YearMonth.from(targetDate)
        val monthsRemaining = (java.time.temporal.ChronoUnit.MONTHS.between(YearMonth.now(), targetYearMonth).toInt() + 1).coerceAtLeast(1)
        if (remaining <= 0) 0.0 else ceil(remaining / monthsRemaining) / 100.0
    }

    val isSaveEnabled = name.isNotBlank() && targetText.toDoubleOrNull()?.let { it > 0 } == true

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text(if (existing == null) "New Sinking Fund" else "Edit Sinking Fund") },
                actions = {
                    if (existing != null && onDelete != null) {
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
                value = name,
                onValueChange = { name = it },
                label = { Text("Fund Name") },
                placeholder = { Text("e.g. Christmas Fund, Emergency Fund") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = targetText,
                onValueChange = { targetText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Target Amount") },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = initialText,
                onValueChange = { initialText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Already Saved (Initial Balance)") },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Need by: ${targetDate.format(displayFormatter)}")
            }

            if (computedMonthly > 0) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(
                        text = "Monthly contribution: ${formatCurrency(computedMonthly)}/mo",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text("Color", style = MaterialTheme.typography.labelMedium)
            ColorPicker(selectedColor = selectedColor, onColorSelected = { selectedColor = it })

            OutlinedTextField(
                value = contributionDayText,
                onValueChange = { v ->
                    contributionDayText = v.filter { it.isDigit() }.take(2)
                    contributionDayText.toIntOrNull()?.let { n ->
                        if (n > 31) contributionDayText = "31"
                        if (n < 1 && contributionDayText.isNotEmpty()) contributionDayText = "1"
                    }
                },
                label = { Text("Auto-contribution Day of Month (optional)") },
                placeholder = { Text("e.g. 15 — leave blank to disable") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (contributionDayText.isNotBlank()) {
                ExposedDropdownMenuBox(expanded = accountMenuExpanded, onExpandedChange = { accountMenuExpanded = it }) {
                    OutlinedTextField(
                        value = accounts.find { it.account.id == selectedAccountId }?.account?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Deduct from Account (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountMenuExpanded) },
                        interactionSource = accountSource,
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = accountMenuExpanded, onDismissRequest = { accountMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = { selectedAccountId = null; accountMenuExpanded = false }
                        )
                        accounts.forEach { awb ->
                            DropdownMenuItem(
                                text = { Text("${awb.account.name} (${awb.account.type.displayName()})") },
                                onClick = { selectedAccountId = awb.account.id; accountMenuExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = categoryMenuExpanded, onExpandedChange = { categoryMenuExpanded = it }) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Transaction Category (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        interactionSource = categorySource,
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = { selectedCategoryId = null; categoryMenuExpanded = false }
                        )
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = { selectedCategoryId = cat.id; categoryMenuExpanded = false }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull() ?: return@Button
                    val initial = initialText.toDoubleOrNull() ?: 0.0
                    val dayOfMonth = contributionDayText.toIntOrNull()?.coerceIn(1, 31)
                    onSave(
                        existing?.id, name, target, initial,
                        targetDate.format(isoFormatter),
                        selectedColor, selectedAccountId, selectedCategoryId, dayOfMonth
                    ) { onNavigateBack() }
                },
                enabled = isSaveEnabled,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = targetDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        targetDate = LocalDate.ofEpochDay(millis / 86400000L)
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
            title = { Text("Delete Fund") },
            text = { Text("Delete \"${existing?.name}\"? All contribution history will also be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    existing?.let { onDelete?.invoke(it.id) { onNavigateBack() } }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}
