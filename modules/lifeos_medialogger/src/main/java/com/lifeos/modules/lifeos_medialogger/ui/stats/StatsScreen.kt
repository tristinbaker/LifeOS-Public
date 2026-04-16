package com.lifeos.modules.lifeos_medialogger.ui.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.modules.lifeos_medialogger.data.local.MediaType
import com.lifeos.modules.lifeos_medialogger.domain.model.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    onTypeClick: (MediaType) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val stats = state.stats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ThisYearCard(stats.thisYearCounts)

        AvgPerMonthCard(stats.thisYearAvgPerMonth, stats.allTimeAvgPerMonth)

        AllTimeTotalCard(stats.allTimeCounts, onTypeClick)

        YearOverYearCard(stats.lastYearCounts, stats.thisYearCounts)

        TopRatedCard(stats.topRated)

        MostConsumedMonthCard(stats.mostConsumedMonth)

        RewatchRegameCard(stats.rewatchRegameCounts)
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun ThisYearCard(counts: YearCounts) {
    StatsCard(title = "This Year", icon = Icons.Default.CalendarToday) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Books", counts.books)
            StatItem("Movies", counts.movies)
            StatItem("Games", counts.games)
        }
    }
}

@Composable
private fun AvgPerMonthCard(thisYear: AvgPerMonth, allTime: AvgPerMonth) {
    StatsCard(title = "Avg Per Month", icon = Icons.Default.Speed) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "This Year",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                AvgRow("Books", thisYear.books)
                AvgRow("Movies", thisYear.movies)
                AvgRow("Games", thisYear.games)
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "All-Time",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                AvgRow("Books", allTime.books)
                AvgRow("Movies", allTime.movies)
                AvgRow("Games", allTime.games)
            }
        }
    }
}

@Composable
private fun AllTimeTotalCard(counts: YearCounts, onTypeClick: (MediaType) -> Unit) {
    StatsCard(title = "All-Time Total", icon = Icons.Default.Inventory) {
        Column {
            TypeTotalRow("Books",  counts.books,  MediaType.BOOK,  onTypeClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            TypeTotalRow("Movies", counts.movies, MediaType.MOVIE, onTypeClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            TypeTotalRow("Games",  counts.games,  MediaType.GAME,  onTypeClick)
        }
    }
}

@Composable
private fun TypeTotalRow(label: String, count: Int, type: MediaType, onClick: (MediaType) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(type) }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "View $label stats",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun YearOverYearCard(lastYear: YearCounts, thisYear: YearCounts) {
    StatsCard(title = "Year Over Year", icon = Icons.AutoMirrored.Filled.TrendingUp) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ComparisonRow("Books", lastYear.books, thisYear.books)
            ComparisonRow("Movies", lastYear.movies, thisYear.movies)
            ComparisonRow("Games", lastYear.games, thisYear.games)
        }
    }
}

@Composable
private fun ComparisonRow(label: String, last: Int, current: Int) {
    val diff = current - last
    val color = when {
        diff > 0 -> MaterialTheme.colorScheme.primary
        diff < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val icon = when {
        diff > 0 -> Icons.AutoMirrored.Filled.TrendingUp
        diff < 0 -> Icons.AutoMirrored.Filled.TrendingDown
        else -> Icons.AutoMirrored.Filled.TrendingFlat
    }
    val sign = when {
        diff > 0 -> "+$diff"
        diff < 0 -> "$diff"
        else -> "0"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(80.dp)
        )
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            sign,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun TopRatedCard(topRated: TopRated) {
    StatsCard(title = "Top Rated", icon = Icons.Default.EmojiEvents) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TopRatedItem("Book", topRated.book)
            TopRatedItem("Movie", topRated.movie)
            TopRatedItem("Game", topRated.game)
        }
    }
}

@Composable
private fun TopRatedItem(label: String, item: TopRatedItem?) {
    if (item == null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(60.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "—",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(60.dp)
            )
            Text(
                item.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(item.rating.toInt()) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                item.dateCompleted?.let { timestamp ->
                    val date = LocalDate.ofInstant(
                        java.time.Instant.ofEpochMilli(timestamp),
                        ZoneId.systemDefault()
                    )
                    val formatted = date.format(DateTimeFormatter.ofPattern("MMM yyyy"))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        formatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MostConsumedMonthCard(monthCount: MonthCount) {
    StatsCard(title = "Most Consumed Month", icon = Icons.Default.CalendarMonth) {
        if (monthCount.count == 0) {
            Text(
                "No data yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Celebration,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "${monthCount.month} ${monthCount.year}:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "${monthCount.count} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RewatchRegameCard(counts: RewatchRegameCounts) {
    StatsCard(title = "Rewatches & Regames", icon = Icons.Default.Replay) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Movie,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Movies rewatched: ${counts.movies}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SportsEsports,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Games regamed: ${counts.games}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
internal fun StatsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

@Composable
private fun StatItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$count",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AvgRow(label: String, avg: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "%.1f/mo".format(avg),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
