package com.lifeos.modules.lifeos_medialogger.ui.books

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File
import com.lifeos.modules.lifeos_medialogger.domain.model.MangaSeries
import com.lifeos.modules.lifeos_medialogger.ui.components.MediaItemCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BooksScreen(
    books: List<com.lifeos.modules.lifeos_medialogger.domain.model.MediaItem>,
    mangaSeries: List<MangaSeries>,
    expandedSeriesIds: Set<Long>,
    onBookClick: (Long) -> Unit,
    onAddBook: () -> Unit,
    onAddSeries: () -> Unit,
    onSeriesClick: (Long) -> Unit,
    onEditSeries: (Long) -> Unit,
    onAddVolume: (Long) -> Unit,
    onEditVolume: (Long) -> Unit
) {
    val booksByYear = books.groupBy { item ->
        item.dateCompleted?.let {
            SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(it))
        } ?: "Unknown"
    }.toSortedMap(compareByDescending { it })

    Scaffold(
        floatingActionButton = {
            var showMenu by remember { mutableStateOf(false) }

            Box {
                SmallFloatingActionButton(
                    onClick = { showMenu = true }
                ) {
                    Icon(Icons.Default.Add, "Add")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add Book") },
                        onClick = {
                            showMenu = false
                            onAddBook()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Manga/Comic Series") },
                        onClick = {
                            showMenu = false
                            onAddSeries()
                        }
                    )
                }
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
            if (mangaSeries.isNotEmpty()) {
                item {
                    Text(
                        "Manga / Comics",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                mangaSeries.forEach { series ->
                    item(key = "series_${series.id}") {
                        MangaSeriesCard(
                            series = series,
                            isExpanded = series.id in expandedSeriesIds,
                            onClick = { onSeriesClick(series.id) },
                            onEditSeries = { onEditSeries(series.id) },
                            onAddVolume = { onAddVolume(series.id) },
                            onEditVolume = onEditVolume
                        )
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            if (booksByYear.isNotEmpty()) {
                booksByYear.forEach { (year, items) ->
                    item(key = "year_$year") {
                        Text(
                            year,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items.forEach { book ->
                        item(key = book.id) {
                            MediaItemCard(
                                item = book,
                                onClick = { onBookClick(book.id) }
                            )
                        }
                    }
                }
            }

            if (books.isEmpty() && mangaSeries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No books or manga/comic logged yet.\nTap + to add one.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MangaSeriesCard(
    series: MangaSeries,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onEditSeries: () -> Unit,
    onAddVolume: () -> Unit,
    onEditVolume: (Long) -> Unit
) {
    val seriesCoverModel = series.coverLocalPath?.let { File(it) } ?: series.coverUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (seriesCoverModel != null) {
                    AsyncImage(
                        model = seriesCoverModel,
                        contentDescription = series.title,
                        modifier = Modifier
                            .size(50.dp, 75.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(series.title, style = MaterialTheme.typography.titleMedium)
                    if (series.author != null) {
                        Text(
                            series.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "${series.volumes.size} volume${if (series.volumes.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditSeries) {
                        Icon(
                            Icons.Default.Edit,
                            "Edit series",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    series.volumes.forEach { volume ->
                        val volumeCoverModel = volume.coverLocalPath?.let { File(it) } ?: volume.coverUrl
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditVolume(volume.id) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (volumeCoverModel != null) {
                                AsyncImage(
                                    model = volumeCoverModel,
                                    contentDescription = "Vol ${volume.volumeNumber}",
                                    modifier = Modifier
                                        .size(32.dp, 48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text("Volume ${volume.volumeNumber}", modifier = Modifier.weight(1f))
                            volume.rating?.let { rating ->
                                Text("${if (rating == rating.toInt().toFloat()) rating.toInt() else rating} / 5")
                            }
                        }
                    }

                    TextButton(onClick = onAddVolume) {
                        Icon(Icons.Default.Add, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Volume")
                    }
                }
            }
        }
    }
}
