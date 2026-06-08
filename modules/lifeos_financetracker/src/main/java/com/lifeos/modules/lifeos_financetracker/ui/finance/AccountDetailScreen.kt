package com.lifeos.modules.lifeos_financetracker.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_financetracker.data.local.*
import com.lifeos.modules.lifeos_financetracker.data.repository.AccountWithBalance
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    awb: AccountWithBalance,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    accounts: List<AccountWithBalance>,
    onNavigateBack: () -> Unit,
    onEditAccount: () -> Unit,
    onUpdateSnapshot: (balance: Double, onComplete: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLiability = !awb.account.type.isAsset()
    val balanceColor = if (isLiability) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
    var showSnapshotDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text(awb.account.name) },
                actions = {
                    IconButton(onClick = onEditAccount) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit account")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance hero
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            awb.account.type.displayName(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            formatCurrency(awb.balance),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = balanceColor
                        )
                        if (isLiability) Text("outstanding balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        else Text("current balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Mortgage details
            if (awb.account.type == AccountType.MORTGAGE && awb.mortgageDetails != null) {
                item {
                    MortgageInfoCard(details = awb.mortgageDetails)
                }
            }

            // Sure up balance — available for all non-mortgage accounts
            if (awb.account.type != AccountType.MORTGAGE) {
                item {
                    OutlinedButton(onClick = { showSnapshotDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Sure Up Balance")
                    }
                }
            }

            // Transaction history
            if (transactions.isNotEmpty()) {
                item {
                    Text("Transaction History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                val groupedTx = transactions.groupBy { it.date }
                val sortedDates = groupedTx.keys.sortedDescending()
                for (date in sortedDates) {
                    item(key = "header_$date") {
                        DateSeparatorHeader(date)
                    }
                    items(groupedTx[date]!!, key = { it.id }) { tx ->
                        val category = categories.find { it.id == tx.categoryId }
                        val account = accounts.find { it.account.id == tx.accountId }?.account
                        TransactionCard(transaction = tx, category = category, account = account, onClick = {})
                    }
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No transactions yet", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (showSnapshotDialog) {
        SnapshotUpdateDialog(
            currentBalance = awb.balance,
            onDismiss = { showSnapshotDialog = false },
            onConfirm = { balance ->
                onUpdateSnapshot(balance) { showSnapshotDialog = false }
            }
        )
    }
}

@Composable
private fun MortgageInfoCard(details: MortgageDetailsEntity, modifier: Modifier = Modifier) {
    val r = details.aprPercent / 100.0 / 12.0
    val payoffDate = if (r > 0) {
        val n = details.remainingMonths
        LocalDate.parse(details.asOfDate).plusMonths(n.toLong())
            .format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    } else "N/A"

    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Mortgage Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            HorizontalDivider()
            MortgageRow("APR", "${details.aprPercent}%")
            MortgageRow("Monthly Payment", formatCurrency(details.monthlyPayment))
            MortgageRow("Remaining Term", "${details.remainingMonths} months")
            MortgageRow("Estimated Payoff", payoffDate)
            MortgageRow("Balance as of", details.asOfDate)
        }
    }
}

@Composable
private fun MortgageRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SnapshotUpdateDialog(
    currentBalance: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var balanceText by remember { mutableStateOf("%.2f".format(currentBalance)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Balance") },
        text = {
            OutlinedTextField(
                value = balanceText,
                onValueChange = { balanceText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Current Balance") },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { balanceText.toDoubleOrNull()?.let { onConfirm(it) } },
                enabled = balanceText.toDoubleOrNull() != null
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
