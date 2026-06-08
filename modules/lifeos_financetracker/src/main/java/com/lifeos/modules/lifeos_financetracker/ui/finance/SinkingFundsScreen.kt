package com.lifeos.modules.lifeos_financetracker.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_financetracker.ui.SinkingFundProgress
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinkingFundsScreen(
    funds: List<SinkingFundProgress>,
    onNavigateBack: () -> Unit,
    onAddFund: () -> Unit,
    onEditFund: (Long) -> Unit,
    onLogContribution: (fundId: Long, amount: Double, date: String, onComplete: () -> Unit) -> Unit,
    onDeleteContribution: (id: Long, onComplete: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var contributionDialogFund by remember { mutableStateOf<SinkingFundProgress?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Sinking Funds") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddFund) {
                Icon(Icons.Default.Add, contentDescription = "Add fund")
            }
        }
    ) { paddingValues ->
        if (funds.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No sinking funds yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                    Text("Tap + to create one", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(funds, key = { it.fund.id }) { progress ->
                    SinkingFundCard(
                        progress = progress,
                        onEdit = { onEditFund(progress.fund.id) },
                        onLogContribution = { contributionDialogFund = progress },
                        onDeleteContribution = onDeleteContribution
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    contributionDialogFund?.let { progress ->
        LogContributionDialog(
            fundName = progress.fund.name,
            onDismiss = { contributionDialogFund = null },
            onConfirm = { amount ->
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                onLogContribution(progress.fund.id, amount, today) { contributionDialogFund = null }
            }
        )
    }
}

@Composable
private fun SinkingFundCard(
    progress: SinkingFundProgress,
    onEdit: () -> Unit,
    onLogContribution: () -> Unit,
    onDeleteContribution: (id: Long, onComplete: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val fundColor = parseColor(progress.fund.colorHex)
    val isFunded = progress.currentSavedCents >= progress.fund.targetAmountCents
    var showContributions by remember { mutableStateOf(false) }
    val targetDateDisplay = try {
        LocalDate.parse(progress.fund.targetDate).format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    } catch (e: Exception) { progress.fund.targetDate }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(fundColor))
                    Text(progress.fund.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onEdit) { Text("Edit") }
            }

            LinearProgressIndicator(
                progress = { progress.progressFraction },
                modifier = Modifier.fillMaxWidth(),
                color = if (isFunded) Color(0xFF4CAF50) else fundColor,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        "${formatCurrency(progress.currentSavedCents / 100.0)} saved",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "of ${formatCurrency(progress.fund.targetAmountCents / 100.0)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (!isFunded) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${formatCurrency(progress.monthlyContributionCents / 100.0)}/mo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = fundColor
                        )
                        Text(
                            "by $targetDateDisplay",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    Text("Goal reached!", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onLogContribution, modifier = Modifier.weight(1f)) {
                    Text("Log Contribution")
                }
                if (progress.contributions.isNotEmpty()) {
                    OutlinedButton(onClick = { showContributions = !showContributions }, modifier = Modifier.weight(1f)) {
                        Text(if (showContributions) "Hide History" else "View History")
                    }
                }
            }

            if (showContributions) {
                HorizontalDivider()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Contribution History", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    progress.contributions.forEach { contribution ->
                        val displayDate = try {
                            LocalDate.parse(contribution.date).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                        } catch (e: Exception) { contribution.date }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(displayDate, style = MaterialTheme.typography.bodySmall)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    formatCurrency(contribution.amountCents / 100.0),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                IconButton(
                                    onClick = { onDeleteContribution(contribution.id) {} },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogContributionDialog(
    fundName: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Contribution") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Add contribution to \"$fundName\"", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount") },
                    prefix = { Text("$") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { amountText.toDoubleOrNull()?.let { onConfirm(it) } },
                enabled = amountText.toDoubleOrNull()?.let { it > 0 } == true
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
