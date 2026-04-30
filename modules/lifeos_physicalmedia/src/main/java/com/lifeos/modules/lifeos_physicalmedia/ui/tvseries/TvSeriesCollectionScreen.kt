package com.lifeos.modules.lifeos_physicalmedia.ui.tvseries

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lifeos.modules.lifeos_physicalmedia.domain.model.PhysicalTvSeries
import com.lifeos.modules.lifeos_physicalmedia.domain.model.displayName

private enum class TvSortMode { BY_TITLE, BY_SERIES }

@Composable
fun TvSeriesCollectionScreen(
    tvSeries: List<PhysicalTvSeries>,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit
) {
    var sortMode by remember { mutableStateOf(TvSortMode.BY_TITLE) }
    var searchQuery by remember { mutableStateOf("") }
    var allExpanded by remember { mutableStateOf(true) }

    val filtered = remember(tvSeries, searchQuery) {
        if (searchQuery.isBlank()) tvSeries
        else tvSeries.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    (it.seriesName?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    val grouped: Map<String, List<PhysicalTvSeries>> = remember(filtered, sortMode) {
        when (sortMode) {
            TvSortMode.BY_TITLE -> filtered
                .sortedBy { it.title.lowercase() }
                .groupBy { it.title.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
                .entries.sortedWith(compareBy { if (it.key == "#") "zzz" else it.key.lowercase() })
                .associate { it.key to it.value }
            TvSortMode.BY_SERIES -> {
                val withSeries = filtered.filter { it.seriesName != null }
                    .sortedWith(compareBy({ it.seriesName!!.lowercase() }, { it.seriesNumber ?: Float.MAX_VALUE }))
                    .groupBy { it.seriesName!! }
                val standalone = filtered.filter { it.seriesName == null }.sortedBy { it.title.lowercase() }
                val result = withSeries.toMutableMap()
                if (standalone.isNotEmpty()) result["Standalone"] = standalone
                result
            }
        }
    }

    val expandedGroups = remember(grouped, allExpanded) {
        mutableStateMapOf<String, Boolean>().also { map ->
            grouped.keys.forEach { key -> map[key] = allExpanded }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, "Add TV Series")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search TV series…") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = sortMode == TvSortMode.BY_TITLE,
                        onClick = { sortMode = TvSortMode.BY_TITLE },
                        label = { Text("By Title") },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    FilterChip(
                        selected = sortMode == TvSortMode.BY_SERIES,
                        onClick = { sortMode = TvSortMode.BY_SERIES },
                        label = { Text("By Series") }
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        allExpanded = !allExpanded
                        grouped.keys.forEach { expandedGroups[it] = !allExpanded.not() }
                    }) {
                        Text(if (expandedGroups.values.any { it }) "Collapse All" else "Expand All")
                    }
                }
            }

            grouped.forEach { (group, items) ->
                val isExpanded = expandedGroups[group] != false
                item(key = "header_$group") {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedGroups[group] = !isExpanded }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${items.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (isExpanded) {
                    items(items, key = { it.id }) { show ->
                        TvSeriesRow(show = show, onClick = { onEdit(show.id) })
                        HorizontalDivider(modifier = Modifier.padding(start = 80.dp))
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No results for \"$searchQuery\""
                            else "No TV series yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvSeriesRow(show: PhysicalTvSeries, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val coverModel = show.coverLocalPath?.takeIf { it.isNotBlank() }
            ?: show.coverUrl?.takeIf { it.isNotBlank() }
        if (coverModel != null) {
            AsyncImage(
                model = coverModel,
                contentDescription = null,
                modifier = Modifier.size(56.dp, 56.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
        } else {
            Spacer(Modifier.width(68.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(show.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            val meta = buildString {
                append(show.format.displayName())
                if (show.completeSeries) append(" · Complete ✓")
                if (show.seriesName != null) {
                    append(" · ${show.seriesName}")
                    show.seriesNumber?.let { n ->
                        val display = if (n == n.toLong().toFloat()) "#${n.toInt()}" else "#$n"
                        append(" $display")
                    }
                }
            }
            Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
