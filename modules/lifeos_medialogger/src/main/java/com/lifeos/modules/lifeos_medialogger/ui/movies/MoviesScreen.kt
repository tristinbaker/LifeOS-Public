package com.lifeos.modules.lifeos_medialogger.ui.movies

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
fun MoviesScreen(
    movies: List<MediaItem>,
    onMovieClick: (Long) -> Unit,
    onAddMovie: () -> Unit
) {
    val moviesByYear = movies.groupBy { movie ->
        movie.dateCompleted?.let {
            SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(it))
        } ?: "Unknown"
    }.toSortedMap(compareByDescending { it })

    var collapsedYears by rememberSaveable { mutableStateOf(emptySet<String>()) }

    Scaffold(
        floatingActionButton = {
            SmallFloatingActionButton(onClick = onAddMovie) {
                Icon(Icons.Default.Add, "Add Movie")
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
            if (moviesByYear.isNotEmpty()) {
                moviesByYear.forEach { (year, items) ->
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
                        items.forEach { movie ->
                            item(key = movie.id) {
                                MediaItemCard(
                                    item = movie,
                                    onClick = { onMovieClick(movie.id) }
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
                            "No movies logged yet.\nTap + to add one.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
