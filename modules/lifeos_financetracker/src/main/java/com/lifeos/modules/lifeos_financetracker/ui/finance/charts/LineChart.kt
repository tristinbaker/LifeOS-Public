package com.lifeos.modules.lifeos_financetracker.ui.finance.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_financetracker.data.local.NetWorthSnapshotEntity
import com.lifeos.modules.lifeos_financetracker.ui.finance.formatCurrency
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LineChart(
    snapshots: List<NetWorthSnapshotEntity>,
    lineColor: Color = Color(0xFF4CAF50),
    modifier: Modifier = Modifier
) {
    if (snapshots.size < 2) {
        Box(modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
            Text("Not enough history yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        return
    }

    val parsed = remember(snapshots) {
        snapshots.mapNotNull {
            try { LocalDate.parse(it.date) to it.netWorth } catch (e: Exception) { null }
        }.sortedBy { it.first }
    }

    if (parsed.size < 2) return

    val minVal = parsed.minOf { it.second }
    val maxVal = parsed.maxOf { it.second }
    val range = (maxVal - minVal).coerceAtLeast(1.0)

    val displayFormatter = DateTimeFormatter.ofPattern("MMM d")
    val startLabel = parsed.first().first.format(displayFormatter)
    val endLabel = parsed.last().first.format(displayFormatter)

    val isPositive = parsed.last().second >= 0
    val actualLineColor = if (isPositive) lineColor else Color(0xFFEF5350)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val w = size.width
            val h = size.height
            val n = parsed.size

            fun xOf(idx: Int) = idx.toFloat() / (n - 1) * w
            fun yOf(value: Double) = ((maxVal - value) / range * h).toFloat().coerceIn(0f, h)

            // Gradient fill path
            val fillPath = Path().apply {
                moveTo(xOf(0), h)
                parsed.forEachIndexed { i, (_, v) -> lineTo(xOf(i), yOf(v)) }
                lineTo(xOf(n - 1), h)
                close()
            }
            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(actualLineColor.copy(alpha = 0.25f), Color.Transparent),
                    startY = 0f, endY = h
                )
            )

            // Line path
            val linePath = Path().apply {
                parsed.forEachIndexed { i, (_, v) ->
                    if (i == 0) moveTo(xOf(i), yOf(v)) else lineTo(xOf(i), yOf(v))
                }
            }
            drawPath(
                linePath,
                color = actualLineColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // End dot
            drawCircle(actualLineColor, radius = 4.dp.toPx(), center = Offset(xOf(n - 1), yOf(parsed.last().second)))
            drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(xOf(n - 1), yOf(parsed.last().second)))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(startLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(
                formatCurrency(parsed.last().second),
                style = MaterialTheme.typography.labelSmall,
                color = actualLineColor
            )
            Text(endLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
