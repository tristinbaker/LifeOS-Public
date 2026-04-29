package com.lifeos.modules.lifeos_physicalmedia.ui.stats

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
import com.lifeos.modules.lifeos_physicalmedia.domain.model.PhysicalMediaStats
import com.lifeos.modules.lifeos_physicalmedia.domain.model.displayName

@Composable
fun PhysicalStatsScreen(stats: PhysicalMediaStats) {
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
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("Added This Year", stats.addedThisYear)
                StatItem("All Time", stats.totalItems)
            }
        }

        StatsCard(title = "Books by Format", icon = Icons.Default.Book) {
            BookFormat.entries.forEach { fmt ->
                StatRow(fmt.displayName(), stats.booksByFormat[fmt] ?: 0)
            }
        }

        StatsCard(title = "Movies by Format", icon = Icons.Default.Movie) {
            MovieFormat.entries.forEach { fmt ->
                StatRow(fmt.displayName(), stats.moviesByFormat[fmt] ?: 0)
            }
            if (stats.steelbookCount > 0 || stats.limitedEditionCount > 0) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StatRow("Steelbooks", stats.steelbookCount)
                StatRow("Limited Editions", stats.limitedEditionCount)
            }
        }

        if (stats.gamesBySystem.isNotEmpty()) {
            StatsCard(title = "Games by System", icon = Icons.Default.SportsEsports) {
                val top3 = stats.top3Systems.map { it.first }.toSet()
                stats.gamesBySystem.entries
                    .sortedByDescending { it.value }
                    .forEach { (system, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
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
                            Text(
                                count.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
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
private fun StatRow(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
