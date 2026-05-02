package com.lifeos.modules.lifeos_financetracker.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_financetracker.ui.FinanceUiState
import com.lifeos.modules.lifeos_financetracker.ui.MonthlyTrend
import com.lifeos.modules.lifeos_financetracker.ui.finance.charts.BarChart
import com.lifeos.modules.lifeos_financetracker.ui.finance.charts.LineChart
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(
    uiState: FinanceUiState,
    onNavigateBack: () -> Unit
) {
    val trends = uiState.monthlyTrends
    val current = trends.lastOrNull()
    val previous = if (trends.size >= 2) trends[trends.size - 2] else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trends") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // MoM Summary
            if (current != null && previous != null) {
                item {
                    Text(
                        "Month over Month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MomCard(
                            label = "Income",
                            current = current.income,
                            previous = previous.income,
                            positiveIsGood = true,
                            modifier = Modifier.weight(1f)
                        )
                        MomCard(
                            label = "Expenses",
                            current = current.expenses,
                            previous = previous.expenses,
                            positiveIsGood = false,
                            modifier = Modifier.weight(1f)
                        )
                        MomCard(
                            label = "Net",
                            current = current.net,
                            previous = previous.net,
                            positiveIsGood = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Income vs Expenses bar chart
            item {
                Text(
                    "Income vs. Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    if (trends.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No transactions yet", color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        BarChart(
                            trends = trends,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Net Worth trend
            item {
                Text(
                    "Net Worth Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    LineChart(
                        snapshots = uiState.netWorthHistory,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MomCard(
    label: String,
    current: Double,
    previous: Double,
    positiveIsGood: Boolean,
    modifier: Modifier = Modifier
) {
    val delta = current - previous
    val pct = if (previous != 0.0) (delta / abs(previous) * 100) else if (current != 0.0) 100.0 else 0.0
    val isUp = delta > 0
    val isGood = if (positiveIsGood) isUp else !isUp
    val color = when {
        delta == 0.0 -> MaterialTheme.colorScheme.outline
        isGood -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (delta != 0.0) {
                    Icon(
                        imageVector = if (isUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = "${if (isUp && delta != 0.0) "+" else ""}${formatPct(pct)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(
                text = formatCurrency(current),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatPct(pct: Double): String {
    val rounded = pct.toInt()
    return "$rounded%"
}
