package com.lifeos.modules.lifeos_financetracker.ui.finance.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_financetracker.ui.MonthlyTrend
import java.time.format.DateTimeFormatter

@Composable
fun BarChart(
    trends: List<MonthlyTrend>,
    incomeColor: Color = Color(0xFF4CAF50),
    expenseColor: Color = Color(0xFFEF5350),
    modifier: Modifier = Modifier
) {
    if (trends.isEmpty()) return

    val maxValue = trends.maxOf { maxOf(it.income, it.expenses) }.coerceAtLeast(1.0).toFloat()
    val monthFormatter = DateTimeFormatter.ofPattern("MMM")
    val gridColor = Color(0x22000000)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val w = size.width
            val h = size.height
            val groupCount = trends.size
            val groupW = w / groupCount
            val barGap = (groupW * 0.1f).coerceAtLeast(2.dp.toPx())
            val barW = ((groupW - barGap * 3f) / 2f).coerceAtLeast(1.dp.toPx())

            // Horizontal grid lines
            repeat(4) { i ->
                val y = h * (1f - (i + 1) / 4f)
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
            }

            trends.forEachIndexed { idx, trend ->
                val groupLeft = idx * groupW

                val incomeH = ((trend.income.toFloat() / maxValue) * h).coerceAtLeast(if (trend.income > 0) 3.dp.toPx() else 0f)
                val expenseH = ((trend.expenses.toFloat() / maxValue) * h).coerceAtLeast(if (trend.expenses > 0) 3.dp.toPx() else 0f)

                drawRect(
                    color = incomeColor,
                    topLeft = Offset(groupLeft + barGap, h - incomeH),
                    size = Size(barW, incomeH)
                )
                drawRect(
                    color = expenseColor,
                    topLeft = Offset(groupLeft + barGap * 2 + barW, h - expenseH),
                    size = Size(barW, expenseH)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            trends.forEach { trend ->
                Text(
                    text = trend.month.format(monthFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(incomeColor))
            Spacer(Modifier.width(4.dp))
            Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
            Box(Modifier.size(8.dp).clip(CircleShape).background(expenseColor))
            Spacer(Modifier.width(4.dp))
            Text("Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
