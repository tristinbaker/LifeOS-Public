package com.lifeos.modules.lifeos_medialogger.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.modules.lifeos_medialogger.data.local.MediaType
import com.lifeos.modules.lifeos_medialogger.domain.model.DetailStats
import com.lifeos.modules.lifeos_medialogger.domain.model.MonthCount
import com.lifeos.modules.lifeos_medialogger.domain.model.TopRatedItem
import com.lifeos.modules.lifeos_medialogger.ui.components.MediaItemCard
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsDetailScreen(
    viewModel: StatsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val detail = state.detailStats ?: return

    val typeName = when (detail.type) {
        MediaType.BOOK  -> "Books"
        MediaType.MOVIE -> "Movies"
        MediaType.GAME  -> "Games"
    }
    val yearLabel = detail.year?.toString() ?: "All Time"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$typeName — $yearLabel") },
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
            // Year / All Time chips
            item(key = "year_chips") {
                YearChipRow(
                    availableYears = detail.availableYears,
                    selectedYear = detail.year,
                    onYearSelected = { viewModel.setDetailYear(it) }
                )
            }

            // Overview
            item(key = "overview") { OverviewCard(detail) }

            // Rating distribution
            if (detail.ratingDistribution.isNotEmpty()) {
                item(key = "rating_dist") { RatingDistributionCard(detail.ratingDistribution) }
            }

            // Top rated
            if (detail.topRated != null) {
                item(key = "top_rated") { DetailTopRatedCard(detail.topRated) }
            }

            // Most active month
            if (detail.mostActiveMonth.count > 0) {
                item(key = "most_active") { DetailMostActiveMonthCard(detail.mostActiveMonth) }
            }

            // Rewatch / Regame breakdown (movies and games only)
            if (detail.type == MediaType.MOVIE || detail.type == MediaType.GAME) {
                item(key = "rewatch") { RewatchCard(detail) }
            }

            // Platform breakdown (games only)
            if (detail.type == MediaType.GAME && detail.platformBreakdown.isNotEmpty()) {
                item(key = "platform") { PlatformCard(detail.platformBreakdown) }
            }

            // Completion flags (games only)
            if (detail.type == MediaType.GAME) {
                item(key = "completion") {
                    CompletionCard(
                        platinumCount = detail.platinumCount,
                        hundredPercentCount = detail.hundredPercentCount,
                        totalCount = detail.totalCount
                    )
                }
            }

            // Top authors (books only)
            if (detail.type == MediaType.BOOK && detail.topAuthors.isNotEmpty()) {
                item(key = "authors") { TopAuthorsCard(detail.topAuthors) }
            }

            // Item list header
            item(key = "list_header") {
                Text(
                    "$typeName (${detail.totalCount})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Item list
            items(detail.items, key = { it.id }) { item ->
                MediaItemCard(item = item, onClick = {})
            }

            item(key = "bottom_spacer") { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun YearChipRow(
    availableYears: List<Int>,
    selectedYear: Int?,
    onYearSelected: (Int?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "all_time") {
            FilterChip(
                selected = selectedYear == null,
                onClick = { onYearSelected(null) },
                label = { Text("All Time") }
            )
        }
        items(availableYears, key = { it }) { year ->
            FilterChip(
                selected = selectedYear == year,
                onClick = { onYearSelected(year) },
                label = { Text("$year") }
            )
        }
    }
}

@Composable
private fun OverviewCard(detail: DetailStats) {
    val label = when (detail.type) {
        MediaType.BOOK  -> "Books Read"
        MediaType.MOVIE -> "Movies Watched"
        MediaType.GAME  -> "Games Played"
    }
    StatsCard(title = "Overview", icon = Icons.Default.Assessment) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DetailStatItem(label, detail.totalCount.toString())
            detail.avgRating?.let {
                DetailStatItem("Avg Rating", "%.1f★".format(it))
            }
            if ((detail.type == MediaType.MOVIE || detail.type == MediaType.GAME) && detail.rewatchCount > 0) {
                val rewatchLabel = if (detail.type == MediaType.MOVIE) "Rewatches" else "Replays"
                DetailStatItem(rewatchLabel, "${detail.rewatchCount}")
            }
        }
    }
}

@Composable
private fun RatingDistributionCard(distribution: Map<Float, Int>) {
    val maxCount = distribution.values.maxOrNull() ?: return
    StatsCard(title = "Rating Distribution", icon = Icons.Default.BarChart) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(5f, 4.5f, 4f, 3.5f, 3f, 2.5f, 2f, 1.5f, 1f, 0.5f).forEach { rating ->
                val count = distribution[rating] ?: 0
                if (count > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "%.1f★".format(rating),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.width(36.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(count.toFloat() / maxCount)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "$count",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.width(24.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTopRatedCard(item: TopRatedItem) {
    StatsCard(title = "Top Rated", icon = Icons.Default.EmojiEvents) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "%.1f★".format(item.rating),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                item.dateCompleted?.let { ts ->
                    val date = LocalDate.ofInstant(
                        java.time.Instant.ofEpochMilli(ts), ZoneId.systemDefault()
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        date.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailMostActiveMonthCard(month: MonthCount) {
    StatsCard(title = "Most Active Month", icon = Icons.Default.CalendarMonth) {
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
            Spacer(Modifier.width(12.dp))
            Text(
                "${month.month} ${month.year}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "· ${month.count} items",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RewatchCard(detail: DetailStats) {
    val title = if (detail.type == MediaType.MOVIE) "Rewatches" else "Replays"
    val uniqueLabel = if (detail.type == MediaType.MOVIE) "Unique Films" else "Unique Games"
    val rewatchLabel = if (detail.type == MediaType.MOVIE) "Rewatches" else "Replays"
    val unique = detail.totalCount - detail.rewatchCount

    StatsCard(title = title, icon = Icons.Default.Replay) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DetailStatItem(uniqueLabel, "$unique")
            DetailStatItem(rewatchLabel, "${detail.rewatchCount}")
            if (detail.rewatchPercent > 0f) {
                DetailStatItem("% Repeat", "${detail.rewatchPercent.toInt()}%")
            }
        }
    }
}

@Composable
private fun PlatformCard(platforms: List<Pair<String, Int>>) {
    StatsCard(title = "By Platform", icon = Icons.Default.Devices) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            platforms.forEach { (platform, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(platform, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "$count",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionCard(platinumCount: Int, hundredPercentCount: Int, totalCount: Int) {
    StatsCard(title = "Completion", icon = Icons.Default.EmojiEvents) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$platinumCount",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Platinum",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (totalCount > 0) {
                    Text(
                        "${(platinumCount * 100 / totalCount)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$hundredPercentCount",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "100%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (totalCount > 0) {
                    Text(
                        "${(hundredPercentCount * 100 / totalCount)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TopAuthorsCard(authors: List<Pair<String, Int>>) {
    StatsCard(title = "Top Authors", icon = Icons.Default.Person) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            authors.forEach { (author, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        author,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "$count",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
