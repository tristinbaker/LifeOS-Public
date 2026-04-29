package com.lifeos.modules.lifeos_medialogger.ui.books

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    onEditVolume: (Long) -> Unit,
    collapsedYears: Set<String> = emptySet(),
    onCollapsedYearsChange: (Set<String>) -> Unit = {},
    collapsedMonths: Set<String> = emptySet(),
    onCollapsedMonthsChange: (Set<String>) -> Unit = {},
    listState: LazyListState = rememberLazyListState()
) {

    // Resolve series completion date and average rating
    val seriesWithDate = mangaSeries.map { series ->
        val seriesDate = series.dateCompleted
            ?: series.volumes.mapNotNull { it.dateCompleted }.maxOrNull()
        val seriesRating = series.volumes
            .mapNotNull { it.rating }
            .average()
            .takeIf { !it.isNaN() }
            ?.toFloat()
        Triple(series, seriesDate, seriesRating)
    }

    // Build year -> monthKey -> items map
    val itemsByYearMonth: Map<String, Map<String, List<Any>>> = buildMap {
        books.forEach { book ->
            val year = book.dateCompleted?.let {
                SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(it))
            } ?: "Unknown"
            val monthKey = book.dateCompleted?.let {
                SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it))
            } ?: "Unknown-00"
            @Suppress("UNCHECKED_CAST")
            val yearMap = getOrPut(year) { mutableMapOf<String, MutableList<Any>>() }
                    as MutableMap<String, MutableList<Any>>
            yearMap.getOrPut(monthKey) { mutableListOf() }.add(BookItem(book))
        }
        seriesWithDate.forEach { (series, seriesDate, seriesRating) ->
            val year = seriesDate?.let {
                SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(it))
            } ?: "Unknown"
            val monthKey = seriesDate?.let {
                SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it))
            } ?: "Unknown-00"
            @Suppress("UNCHECKED_CAST")
            val yearMap = getOrPut(year) { mutableMapOf<String, MutableList<Any>>() }
                    as MutableMap<String, MutableList<Any>>
            yearMap.getOrPut(monthKey) { mutableListOf() }.add(SeriesItem(series, seriesDate, seriesRating))
        }
    }.mapValues { (_, monthMap) ->
        monthMap.mapValues { (_, items) ->
            val sorted = items.sortedByDescending { item ->
                when (item) {
                    is BookItem -> item.book.dateCompleted ?: 0L
                    is SeriesItem -> item.date ?: 0L
                    else -> 0L
                }
            }
            // Within the same series, re-order books by series number ascending
            val seriesGroups = sorted.filterIsInstance<BookItem>()
                .filter { it.book.seriesName != null }
                .groupBy { it.book.seriesName!! }
            val seriesPositions = mutableMapOf<String, Int>()
            sorted.forEachIndexed { i, item ->
                if (item is BookItem && item.book.seriesName != null) {
                    seriesPositions.getOrPut(item.book.seriesName) { i }
                }
            }
            val nonSeriesItems = sorted.filter { it !is BookItem || it.book.seriesName == null }
            val reordered = nonSeriesItems.toMutableList<Any>()
            seriesPositions.keys.sorted().forEach { name ->
                val group = seriesGroups[name]?.sortedBy { it.book.seriesNumber ?: Float.MAX_VALUE } ?: return@forEach
                val insertAt = minOf(seriesPositions[name]!!, reordered.size)
                reordered.addAll(insertAt, group)
            }
            reordered
        }.toSortedMap(compareByDescending { it })
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
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsByYearMonth.forEach { (year, monthMap) ->
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
                            "$year ($yearCount ${if (yearCount == 1) "Book" else "Books"})",
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
                                    "$monthLabel ($monthCount ${if (monthCount == 1) "Book" else "Books"})",
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
                            items.forEach { item ->
                                when (item) {
                                    is BookItem -> {
                                        item(key = item.book.id) {
                                            MediaItemCard(
                                                item = item.book,
                                                onClick = { onBookClick(item.book.id) }
                                            )
                                        }
                                    }
                                    is SeriesItem -> {
                                        item(key = "series_${item.series.id}") {
                                            MangaSeriesCard(
                                                series = item.series,
                                                isExpanded = item.series.id in expandedSeriesIds,
                                                onSeriesClick = { onSeriesClick(item.series.id) },
                                                onEditSeries = { onEditSeries(item.series.id) },
                                                onAddVolume = { onAddVolume(item.series.id) },
                                                onEditVolume = onEditVolume
                                            )
                                        }
                                    }
                                    else -> {}
                                }
                            }
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

private data class BookItem(val book: com.lifeos.modules.lifeos_medialogger.domain.model.MediaItem)
private data class SeriesItem(
    val series: MangaSeries,
    val date: Long?,
    val rating: Float?
)

@Composable
fun MangaSeriesCard(
    series: MangaSeries,
    isExpanded: Boolean,
    onSeriesClick: () -> Unit,
    onEditSeries: () -> Unit,
    onAddVolume: () -> Unit,
    onEditVolume: (Long) -> Unit
) {
    val seriesCoverModel = series.coverLocalPath?.let { File(it) } ?: series.coverUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSeriesClick)
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
