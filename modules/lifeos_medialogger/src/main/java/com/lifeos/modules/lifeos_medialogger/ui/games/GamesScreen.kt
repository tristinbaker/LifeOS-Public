package com.lifeos.modules.lifeos_medialogger.ui.games

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_medialogger.domain.model.MediaItem
import com.lifeos.modules.lifeos_medialogger.ui.components.MediaItemCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GamesScreen(
    games: List<MediaItem>,
    onGameClick: (Long) -> Unit,
    onAddGame: () -> Unit
) {
    val gamesByYear = games.groupBy { game ->
        game.dateCompleted?.let {
            SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(it))
        } ?: "Unknown"
    }.toSortedMap(compareByDescending { it })

    var collapsedYears by rememberSaveable { mutableStateOf(emptySet<String>()) }

    Scaffold(
        floatingActionButton = {
            SmallFloatingActionButton(onClick = onAddGame) {
                Icon(Icons.Default.Add, "Add Game")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (gamesByYear.isNotEmpty()) {
                gamesByYear.forEach { (year, items) ->
                    val isCollapsed = year in collapsedYears
                    item(key = "year_$year") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    collapsedYears = if (isCollapsed)
                                        collapsedYears - year
                                    else
                                        collapsedYears + year
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(year, style = MaterialTheme.typography.titleMedium)
                            Icon(
                                imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = if (isCollapsed) "Expand" else "Collapse",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isCollapsed) {
                        items.forEach { game ->
                            item(key = game.id) {
                                MediaItemCard(
                                    item = game,
                                    onClick = { onGameClick(game.id) }
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No games logged yet.\nTap + to add one.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
