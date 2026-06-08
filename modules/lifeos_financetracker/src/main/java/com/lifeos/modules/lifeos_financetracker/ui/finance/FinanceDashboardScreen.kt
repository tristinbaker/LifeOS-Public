package com.lifeos.modules.lifeos_financetracker.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_financetracker.data.local.*
import com.lifeos.modules.lifeos_financetracker.data.repository.AccountWithBalance
import com.lifeos.modules.lifeos_financetracker.data.repository.CategorySpending
import com.lifeos.modules.lifeos_financetracker.ui.FinanceUiState
import com.lifeos.modules.lifeos_financetracker.ui.SinkingFundProgress
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceDashboardScreen(
    uiState: FinanceUiState,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onAddAccount: () -> Unit,
    onAccountClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onRecurringClick: () -> Unit,
    onTrendsClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    onSinkingFundsClick: () -> Unit,
    onPersonalCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentMonth = uiState.selectedMonth == YearMonth.now()
    val monthLabel = uiState.selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    val assetAccounts = uiState.accounts.filter { it.account.type.isAsset() }
    val liabilityAccounts = uiState.accounts.filter { !it.account.type.isAsset() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Finance") },
                actions = {
                    IconButton(onClick = onTrendsClick) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Trends")
                    }
                    IconButton(onClick = onRecurringClick) {
                        Icon(Icons.Default.Repeat, contentDescription = "Recurring")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Add transaction")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Net Worth Hero
            item {
                NetWorthCard(
                    netWorth = uiState.netWorth,
                    totalAssets = uiState.totalAssets,
                    totalLiabilities = uiState.totalLiabilities
                )
            }

            // Accounts section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Accounts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = onAddAccount) {
                        Icon(Icons.Default.Add, contentDescription = "Add account")
                    }
                }
            }

            if (uiState.accounts.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No accounts yet. Tap + to add one.", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                if (assetAccounts.isNotEmpty()) {
                    item {
                        Text("Assets", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    }
                    items(assetAccounts, key = { "asset_${it.account.id}" }) { awb ->
                        AccountRow(awb = awb, onClick = { onAccountClick(awb.account.id) })
                    }
                }
                if (liabilityAccounts.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text("Liabilities", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    }
                    items(liabilityAccounts, key = { "liability_${it.account.id}" }) { awb ->
                        AccountRow(awb = awb, isLiability = true, onClick = { onAccountClick(awb.account.id) })
                    }
                }
            }

            // Monthly summary
            item {
                MonthlySummaryCard(
                    monthLabel = monthLabel,
                    totalIncome = uiState.expectedMonthlyIncome,
                    totalExpenses = uiState.totalExpenses,
                    netBalance = uiState.expectedMonthlyIncome - uiState.totalExpenses,
                    isIncomeProjected = uiState.expectedMonthlyIncome > uiState.totalIncome
                )
            }

            if (uiState.categorySpending.isNotEmpty()) {
                item {
                    Text("Spending", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.categorySpending, key = { it.category.id }) { item ->
                            CategorySpendingChip(spending = item)
                        }
                    }
                }
            }

            // Sinking Funds section
            if (uiState.sinkingFunds.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sinking Funds", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = onSinkingFundsClick, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Manage sinking funds")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.sinkingFunds, key = { it.fund.id }) { progress ->
                            SinkingFundChip(progress = progress, onClick = onSinkingFundsClick)
                        }
                    }
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sinking Funds", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = onSinkingFundsClick, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Add sinking fund")
                        }
                    }
                    Text(
                        "No sinking funds yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Personal Card
            item {
                PersonalCardSummaryRow(
                    balance = uiState.personalCardBalance,
                    onClick = onPersonalCardClick
                )
            }

            // Transactions with month navigation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onPreviousMonth, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                    }
                    Text(
                        monthLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                    }
                    if (!isCurrentMonth) {
                        TextButton(onClick = onTodayClick, contentPadding = PaddingValues(horizontal = 8.dp)) {
                            Text("Today", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            if (uiState.monthlyTransactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No transactions this month", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                val groupedTx = uiState.monthlyTransactions.groupBy { it.date }
                val sortedDates = groupedTx.keys.sortedDescending()
                for (date in sortedDates) {
                    item(key = "header_$date") {
                        DateSeparatorHeader(date)
                    }
                    items(groupedTx[date]!!, key = { it.id }) { tx ->
                        val account = uiState.accounts.find { it.account.id == tx.accountId }?.account
                        val category = uiState.categories.find { it.id == tx.categoryId }
                        TransactionCard(transaction = tx, category = category, account = account, onClick = { onEditTransaction(tx.id) })
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

}

@Composable
private fun SinkingFundChip(progress: SinkingFundProgress, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val fundColor = parseColor(progress.fund.colorHex)
    val isFunded = progress.currentSavedCents >= progress.fund.targetAmountCents
    Card(
        modifier = modifier.width(150.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(fundColor))
                Text(progress.fund.name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (isFunded) {
                Text("Funded!", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            } else {
                Text(
                    "${formatCurrency(progress.monthlyContributionCents / 100.0)}/mo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = fundColor
                )
                LinearProgressIndicator(
                    progress = { progress.progressFraction },
                    modifier = Modifier.fillMaxWidth(),
                    color = fundColor,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                Text(
                    "${formatCurrency(progress.currentSavedCents / 100.0)} / ${formatCurrency(progress.fund.targetAmountCents / 100.0)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun NetWorthCard(
    netWorth: Double,
    totalAssets: Double,
    totalLiabilities: Double,
    modifier: Modifier = Modifier
) {
    val isPositive = netWorth >= 0
    val netWorthColor = if (isPositive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Net Worth", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = netWorthColor,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = formatCurrency(netWorth),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = netWorthColor
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Assets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(formatCurrency(totalAssets), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF4CAF50))
                }
                VerticalDivider(modifier = Modifier.height(32.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Liabilities", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(formatCurrency(totalLiabilities), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AccountRow(
    awb: AccountWithBalance,
    isLiability: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val balanceColor = if (isLiability) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(parseColor(awb.account.colorHex)))
            Column(modifier = Modifier.weight(1f)) {
                Text(awb.account.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(awb.account.type.displayName(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Text(
                text = formatCurrency(awb.balance),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = balanceColor
            )
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    monthLabel: String,
    totalIncome: Double,
    totalExpenses: Double,
    netBalance: Double,
    isIncomeProjected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(monthLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCell(
                    label = if (isIncomeProjected) "Expected" else "Income",
                    amount = totalIncome,
                    color = Color(0xFF4CAF50),
                    icon = Icons.AutoMirrored.Filled.TrendingUp
                )
                VerticalDivider(modifier = Modifier.height(56.dp))
                StatCell("Expenses", totalExpenses, MaterialTheme.colorScheme.error, Icons.AutoMirrored.Filled.TrendingDown)
                VerticalDivider(modifier = Modifier.height(56.dp))
                StatCell("Net", netBalance, if (netBalance >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    if (netBalance >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown)
            }
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Text(formatCurrency(amount), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun CategorySpendingChip(spending: CategorySpending, modifier: Modifier = Modifier) {
    val categoryColor = parseColor(spending.category.colorHex)
    val budgetProgress = spending.budgetLimitCents?.let { limitCents ->
        if (limitCents > 0) (spending.totalSpent * 100 / limitCents).toFloat().coerceIn(0f, 1f) else 0f
    }
    Card(modifier = modifier.width(140.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(categoryColor))
                Text(spending.category.name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(formatCurrency(spending.totalSpent), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (budgetProgress != null && spending.budgetLimitCents != null) {
                LinearProgressIndicator(
                    progress = { budgetProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (budgetProgress > 0.9f) MaterialTheme.colorScheme.error else categoryColor,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                Text("/ ${formatCurrency(spending.budgetLimitCents / 100.0)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
internal fun DateSeparatorHeader(dateString: String, modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    val date = try { LocalDate.parse(dateString) } catch (e: Exception) { null }
    val label = when {
        date == null -> dateString
        date == today -> "Today"
        date == today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    }
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
internal fun TransactionCard(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    account: AccountEntity?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val isTransfer = transaction.type == TransactionType.TRANSFER
    val amountColor = when {
        isIncome -> Color(0xFF4CAF50)
        isTransfer -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }
    val amountPrefix = if (isIncome) "+" else if (isTransfer) "↕" else "-"
    val categoryColor = category?.let { parseColor(it.colorHex) } ?: MaterialTheme.colorScheme.outline
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(categoryColor))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.note.ifBlank { category?.name ?: "Unknown" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        category?.name ?: "Unknown",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    account?.name ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$amountPrefix${formatCurrency(transaction.amount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = amountColor)
                Text(
                    try { java.time.LocalDate.parse(transaction.date).format(dateFormatter) } catch (e: Exception) { transaction.date },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun PersonalCardSummaryRow(balance: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isOwed = balance > 0
    val balanceColor = if (isOwed) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.CreditCard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Personal Card", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("Resale balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Text(
                formatCurrency(balance),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = balanceColor
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

internal fun formatCurrency(amount: Double): String =
    NumberFormat.getCurrencyInstance(Locale.US).format(amount)

internal fun parseColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) {
    Color.Gray
}
