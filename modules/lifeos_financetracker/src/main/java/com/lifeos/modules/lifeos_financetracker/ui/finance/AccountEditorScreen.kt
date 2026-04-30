package com.lifeos.modules.lifeos_financetracker.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_financetracker.data.local.*
import com.lifeos.modules.lifeos_financetracker.data.repository.AccountWithBalance

private val accountColors = listOf(
    "#4CAF50", "#2196F3", "#FF5722", "#9C27B0",
    "#FF9800", "#00BCD4", "#78909C", "#F44336"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditorScreen(
    existing: AccountWithBalance?,
    onNavigateBack: () -> Unit,
    onSave: (id: Long?, name: String, type: AccountType, colorHex: String, startingBalance: Double, onComplete: (Long?) -> Unit) -> Unit,
    onSaveMortgage: (accountId: Long, currentBalance: Double, aprPercent: Double, remainingMonths: Int, monthlyPayment: Double, onComplete: () -> Unit) -> Unit,
    onDelete: (Long, onComplete: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val account = existing?.account
    var name by remember(account?.id) { mutableStateOf(account?.name ?: "") }
    var selectedType by remember(account?.id) { mutableStateOf(account?.type ?: AccountType.CHECKING) }
    var selectedColor by remember(account?.id) { mutableStateOf(account?.colorHex ?: accountColors.first()) }
    var startingBalanceText by remember(account?.id) { mutableStateOf(account?.startingBalance?.let { "%.2f".format(it) } ?: "") }

    // Mortgage-specific fields
    val mortgage = existing?.mortgageDetails
    var mortgageBalanceText by remember(account?.id) { mutableStateOf(mortgage?.currentBalance?.let { "%.2f".format(it) } ?: "") }
    var aprText by remember(account?.id) { mutableStateOf(mortgage?.aprPercent?.toString() ?: "") }
    var remainingMonthsText by remember(account?.id) { mutableStateOf(mortgage?.remainingMonths?.toString() ?: "") }
    var monthlyPaymentText by remember(account?.id) { mutableStateOf(mortgage?.monthlyPayment?.let { "%.2f".format(it) } ?: "") }

    var typeMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val typeSource = remember { MutableInteractionSource() }
    val typePressed by typeSource.collectIsPressedAsState()
    if (typePressed) typeMenuExpanded = true

    val isMortgage = selectedType == AccountType.MORTGAGE
    val isSnapshot = selectedType.isSnapshot()

    val isSaveEnabled = name.isNotBlank() && when {
        isMortgage -> mortgageBalanceText.toDoubleOrNull() != null &&
                aprText.toDoubleOrNull() != null &&
                remainingMonthsText.toIntOrNull() != null &&
                monthlyPaymentText.toDoubleOrNull() != null
        isSnapshot -> startingBalanceText.toDoubleOrNull() != null || startingBalanceText.isBlank()
        else -> startingBalanceText.toDoubleOrNull() != null || startingBalanceText.isBlank()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text(if (account == null) "Add Account" else "Edit Account") },
                actions = {
                    if (account != null) {
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
                label = { Text("Account Name") },
                placeholder = { Text("e.g. Chase Checking") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = { typeMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedType.displayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Account Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                    interactionSource = typeSource,
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                    AccountType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName()) },
                            onClick = { selectedType = type; typeMenuExpanded = false }
                        )
                    }
                }
            }

            Text("Color", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                accountColors.forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(parseColor(hex))
                            .then(if (hex == selectedColor) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                            .clickable { selectedColor = hex }
                    )
                }
            }

            if (isMortgage) {
                OutlinedTextField(
                    value = mortgageBalanceText,
                    onValueChange = { mortgageBalanceText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Current Balance") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = aprText,
                    onValueChange = { aprText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("APR (%)") },
                    suffix = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = remainingMonthsText,
                    onValueChange = { remainingMonthsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Remaining Term (months)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = monthlyPaymentText,
                    onValueChange = { monthlyPaymentText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Monthly Payment") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (aprText.toDoubleOrNull() != null && remainingMonthsText.toIntOrNull() != null && monthlyPaymentText.toDoubleOrNull() != null) {
                    val years = remainingMonthsText.toInt() / 12
                    val months = remainingMonthsText.toInt() % 12
                    val termStr = if (years > 0 && months > 0) "$years yr $months mo" else if (years > 0) "$years yr" else "$months mo"
                    Text(
                        "Payoff in ~$termStr at ${aprText}% APR",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                OutlinedTextField(
                    value = startingBalanceText,
                    onValueChange = { startingBalanceText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(if (isSnapshot) "Current Balance" else "Starting Balance") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isSnapshot) {
                    Text(
                        "You can update this balance anytime from the account detail screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Button(
                onClick = {
                    val startingBal = startingBalanceText.toDoubleOrNull() ?: 0.0
                    onSave(account?.id, name, selectedType, selectedColor, startingBal) { savedId ->
                        if (isMortgage && savedId != null) {
                            onSaveMortgage(
                                savedId,
                                mortgageBalanceText.toDouble(),
                                aprText.toDouble(),
                                remainingMonthsText.toInt(),
                                monthlyPaymentText.toDouble()
                            ) { onNavigateBack() }
                        } else {
                            onNavigateBack()
                        }
                    }
                },
                enabled = isSaveEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Account")
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove Account") },
            text = { Text("This account will be hidden. Its transactions will be preserved.") },
            confirmButton = {
                TextButton(onClick = {
                    account?.let { onDelete(it.id) { onNavigateBack() } }
                }) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
