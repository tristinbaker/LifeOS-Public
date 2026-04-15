package com.lifeos.modules.lifeos_medialogger.ui.games

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
    onAddGame: () -> Unit,
    collapsedYears: Set<String> = emptySet(),
    onCollapsedYearsChange: (Set<String>) -> Unit = {},
    collapsedMonths: Set<String> = emptySet(),
    onCollapsedMonthsChange: (Set<String>) -> Unit = {},
    listState: LazyListState = rememberLazyListState()
) {
    // year -> monthKey ("2026-04") -> games, sorted newest first within each month
    val gamesByYearMonth: Map<String, Map<String, List<MediaItem>>> = buildMap {
        games.forEach { game ->
            val year = game.dateCompleted?.let {
                SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(it))
            } ?: "Unknown"
            val monthKey = game.dateCompleted?.let {
                SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it))
            } ?: "Unknown-00"
            @Suppress("UNCHECKED_CAST")
            val yearMap = getOrPut(year) { mutableMapOf<String, MutableList<MediaItem>>() }
                    as MutableMap<String, MutableList<MediaItem>>
            yearMap.getOrPut(monthKey) { mutableListOf() }.add(game)
        }
    }.mapValues { (_, monthMap) ->
        monthMap.mapValues { (_, items) ->
            items.sortedWith(compareByDescending<MediaItem> { it.dateCompleted ?: 0L }.thenByDescending { it.createdAt })
        }.toSortedMap(compareByDescending { it })
    }.toSortedMap(compareByDescending { it })


    Scaffold(
        floatingActionButton = {
            SmallFloatingActionButton(onClick = onAddGame) {
                Icon(Icons.Default.Add, "Add Game")
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (gamesByYearMonth.isNotEmpty()) {
                gamesByYearMonth.forEach { (year, monthMap) ->
                    val yearCount = monthMap.values.sumOf { it.size }
                    val isYearCollapsed = year in collapsedYears
                    item(key = "year_$year") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCollapsedYearsChange(if (isYearCollapsed)
                                        collapsedYears - year
                                    else
                                        collapsedYears + year
                                    )
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$year ($yearCount ${if (yearCount == 1) "Game" else "Games"})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Icon(
                                imageVector = if (isYearCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = if (isYearCollapsed) "Expand" else "Collapse",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isYearCollapsed) {
                        monthMap.forEach { (monthKey, items) ->
                            val monthCollapseKey = "$year-$monthKey"
                            val isMonthCollapsed = monthCollapseKey in collapsedMonths
                            val monthLabel = if (monthKey == "Unknown-00") "Unknown" else
                                SimpleDateFormat("MMMM", Locale.getDefault())
                                    .format(SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(monthKey)!!)
                            val monthCount = items.size
                            item(key = "month_$monthCollapseKey") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onCollapsedMonthsChange(if (isMonthCollapsed)
                                                collapsedMonths - monthCollapseKey
                                            else
                                                collapsedMonths + monthCollapseKey
                                            )
                                        }
                                        .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "$monthLabel ($monthCount ${if (monthCount == 1) "Game" else "Games"})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        imageVector = if (isMonthCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                        contentDescription = if (isMonthCollapsed) "Expand" else "Collapse",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (!isMonthCollapsed) {
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
