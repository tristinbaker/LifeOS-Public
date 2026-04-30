package com.lifeos.modules.lifeos_physicalmedia.ui.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_physicalmedia.data.local.BookFormat
import com.lifeos.modules.lifeos_physicalmedia.data.local.MovieFormat
import com.lifeos.modules.lifeos_physicalmedia.domain.model.PhysicalMediaDrillDown
import com.lifeos.modules.lifeos_physicalmedia.domain.model.PhysicalMediaStats
import com.lifeos.modules.lifeos_physicalmedia.domain.model.displayName
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PhysicalStatsScreen(
    stats: PhysicalMediaStats,
    gamePrices: Map<Long, Double?> = emptyMap(),
    isFetchingPrices: Boolean = false,
    pricesFetchCount: Int = 0,
    totalGames: Int = 0,
    onFetchPrices: () -> Unit = {},
    onDrillDown: (PhysicalMediaDrillDown) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatsCard(title = "Collection Overview", icon = Icons.Default.Inventory2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Books", stats.totalBooks)
                StatItem("Movies", stats.totalMovies)
                StatItem("Games", stats.totalGames)
                StatItem("TV", stats.totalTvSeries)
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("Added This Year", stats.addedThisYear)
                StatItem("All Time", stats.totalItems)
            }
        }

        StatsCard(title = "Books by Format", icon = Icons.Default.Book) {
            BookFormat.entries.forEach { fmt ->
                val count = stats.booksByFormat[fmt] ?: 0
                StatRow(fmt.displayName(), count,
                    onClick = if (count > 0) { { onDrillDown(PhysicalMediaDrillDown.BooksByFormat(fmt)) } } else null)
            }
        }

        StatsCard(title = "Movies by Format", icon = Icons.Default.Movie) {
            MovieFormat.entries.forEach { fmt ->
                val count = stats.moviesByFormat[fmt] ?: 0
                StatRow(fmt.displayName(), count,
                    onClick = if (count > 0) { { onDrillDown(PhysicalMediaDrillDown.MoviesByFormat(fmt)) } } else null)
            }
            if (stats.steelbookCount > 0 || stats.limitedEditionCount > 0) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StatRow("Steelbooks", stats.steelbookCount,
                    onClick = if (stats.steelbookCount > 0) { { onDrillDown(PhysicalMediaDrillDown.MoviesSteelbooks) } } else null)
                StatRow("Limited Editions", stats.limitedEditionCount,
                    onClick = if (stats.limitedEditionCount > 0) { { onDrillDown(PhysicalMediaDrillDown.MoviesLimitedEditions) } } else null)
            }
        }

        if (stats.boutiqueLabelCount > 0) {
            StatsCard(title = "Movies by Boutique Label", icon = Icons.Default.LocalOffer) {
                StatRow("Boutique", stats.boutiqueLabelCount,
                    onClick = { onDrillDown(PhysicalMediaDrillDown.MoviesBoutique) })
                StatRow("Standard", stats.standardMovieCount,
                    onClick = if (stats.standardMovieCount > 0) { { onDrillDown(PhysicalMediaDrillDown.MoviesStandard) } } else null)
                if (stats.moviesByBoutiqueLabel.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    stats.moviesByBoutiqueLabel.forEach { (label, count) ->
                        StatRow(label, count, onClick = { onDrillDown(PhysicalMediaDrillDown.MoviesByBoutiqueLabel(label)) })
                    }
                }
            }
        }

        StatsCard(title = "TV by Format", icon = Icons.Default.Tv) {
            MovieFormat.entries.forEach { fmt ->
                val count = stats.tvSeriesByFormat[fmt] ?: 0
                StatRow(fmt.displayName(), count,
                    onClick = if (count > 0) { { onDrillDown(PhysicalMediaDrillDown.TvByFormat(fmt)) } } else null)
            }
            if (stats.completeTvSeriesCount > 0) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StatRow("Complete Series", stats.completeTvSeriesCount,
                    onClick = { onDrillDown(PhysicalMediaDrillDown.TvCompleteSeries) })
            }
        }

        if (stats.gamesBySystem.isNotEmpty()) {
            StatsCard(title = "Games by System", icon = Icons.Default.SportsEsports) {
                val top3 = stats.top3Systems.map { it.first }.toSet()
                stats.gamesBySystem.entries
                    .sortedByDescending { it.value }
                    .forEach { (system, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { onDrillDown(PhysicalMediaDrillDown.GamesBySystem(system)) }
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (system in top3) {
                                    Icon(
                                        Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Spacer(Modifier.size(16.dp))
                                }
                                Text(system.displayName(), style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    count.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
            }
        } else {
            StatsCard(title = "Games by System", icon = Icons.Default.SportsEsports) {
                Text(
                    "No games yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (totalGames > 0) {
            val currency = NumberFormat.getCurrencyInstance(Locale.US)
            val matched = gamePrices.values.filterNotNull()
            val total = matched.sum()
            val hasFetched = gamePrices.isNotEmpty() || (!isFetchingPrices && pricesFetchCount > 0)

            StatsCard(title = "Game Collection Value", icon = Icons.Default.Sell) {
                if (isFetchingPrices) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(
                            progress = { if (totalGames > 0) pricesFetchCount.toFloat() / totalGames else 0f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "Fetching CIB prices… $pricesFetchCount / $totalGames",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (hasFetched) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                currency.format(total),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "${matched.size} of $totalGames games matched (CIB)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onFetchPrices) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh prices")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Estimate CIB sell value",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onFetchPrices) {
                            Text("Fetch")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun StatsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value.toString(),
            style = MaterialTheme.typography.headlineMedium,
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
private fun StatRow(label: String, value: Int, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
