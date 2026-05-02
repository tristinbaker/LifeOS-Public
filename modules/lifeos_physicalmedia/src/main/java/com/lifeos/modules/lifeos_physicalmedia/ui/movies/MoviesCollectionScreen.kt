package com.lifeos.modules.lifeos_physicalmedia.ui.movies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_physicalmedia.domain.model.PhysicalMovie
import com.lifeos.modules.lifeos_physicalmedia.domain.model.displayName
import com.lifeos.modules.lifeos_physicalmedia.ui.components.PhysicalItemRow

enum class MovieSortMode { BY_FORMAT, BY_TITLE, BY_BOUTIQUE_LABEL }

@Composable
fun MoviesCollectionScreen(
    movies: List<PhysicalMovie>,
    onMovieClick: (Long) -> Unit,
    onAddMovie: () -> Unit,
    onRandomMovie: () -> Unit = {},
    listState: LazyListState = rememberLazyListState()
) {
    var sortMode by remember { mutableStateOf(MovieSortMode.BY_FORMAT) }
    var collapsedGroups by remember { mutableStateOf(emptySet<String>()) }
    var expandedCollections by remember { mutableStateOf(emptySet<Long>()) }
    var searchQuery by remember { mutableStateOf("") }

    val sortedGroups: List<Pair<String, List<PhysicalMovie>>> = remember(movies, sortMode, searchQuery) {
        val filtered = if (searchQuery.isBlank()) movies else movies.filter { movie ->
            movie.title.contains(searchQuery, ignoreCase = true) ||
                (movie.boutiqueLabel?.contains(searchQuery, ignoreCase = true) == true)
        }
        when (sortMode) {
            MovieSortMode.BY_FORMAT -> filtered
                .sortedBy { titleSortKey(it.title).lowercase() }
                .groupBy { it.format.displayName() }
                .entries.sortedBy { it.key }
                .map { it.key to it.value }

            MovieSortMode.BY_TITLE -> filtered
                .sortedBy { titleSortKey(it.title).lowercase() }
                .groupBy { groupLetter(titleSortKey(it.title)) }
                .entries.sortedWith(compareBy { if (it.key == "#") "zzz" else it.key })
                .map { it.key to it.value }

            MovieSortMode.BY_BOUTIQUE_LABEL -> {
                val withLabel = filtered.filter { it.boutiqueLabel != null }
                    .sortedBy { titleSortKey(it.title).lowercase() }
                    .groupBy { it.boutiqueLabel!! }
                    .entries.sortedBy { it.key }
                    .map { it.key to it.value }
                val noLabel = filtered.filter { it.boutiqueLabel == null }
                    .sortedBy { titleSortKey(it.title).lowercase() }
                if (noLabel.isNotEmpty()) withLabel + ("No Label" to noLabel)
                else withLabel
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(onClick = onRandomMovie) {
                    Icon(Icons.Default.Casino, "Pick Random Movie")
                }
                SmallFloatingActionButton(onClick = onAddMovie) {
                    Icon(Icons.Default.Add, "Add Movie")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item(key = "search_bar") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search movies…") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            item(key = "sort_chips") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = sortMode == MovieSortMode.BY_FORMAT,
                            onClick = { sortMode = MovieSortMode.BY_FORMAT; collapsedGroups = emptySet() },
                            label = { Text("By Format") }
                        )
                        FilterChip(
                            selected = sortMode == MovieSortMode.BY_TITLE,
                            onClick = { sortMode = MovieSortMode.BY_TITLE; collapsedGroups = emptySet() },
                            label = { Text("By Title") }
                        )
                        FilterChip(
                            selected = sortMode == MovieSortMode.BY_BOUTIQUE_LABEL,
                            onClick = { sortMode = MovieSortMode.BY_BOUTIQUE_LABEL; collapsedGroups = emptySet() },
                            label = { Text("By Label") }
                        )
                    }
                    val allGroupKeys = sortedGroups.map { it.first }.toSet()
                    val allCollapsed = allGroupKeys.isNotEmpty() && allGroupKeys == collapsedGroups
                    TextButton(
                        onClick = { collapsedGroups = if (allCollapsed) emptySet() else allGroupKeys },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            if (allCollapsed) "Expand All" else "Collapse All",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            if (movies.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No movies yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            sortedGroups.forEach { (groupKey, groupMovies) ->
                val isCollapsed = groupKey in collapsedGroups
                item(key = "group_$groupKey") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                collapsedGroups = if (isCollapsed)
                                    collapsedGroups - groupKey
                                else
                                    collapsedGroups + groupKey
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$groupKey  (${groupMovies.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider()
                }

                if (!isCollapsed) {
                    groupMovies.forEach { movie ->
                        item(key = "movie_${movie.id}") {
                            PhysicalItemRow(
                                coverLocalPath = movie.coverLocalPath,
                                coverUrl = movie.coverUrl,
                                title = movie.title,
                                metadata = buildString {
                                    append(movie.format.displayName())
                                    if (movie.boutiqueLabel != null) {
                                        append(" · ${movie.boutiqueLabel}")
                                        if (movie.catalogNumber != null) append(" (${movie.catalogNumber})")
                                    }
                                    if (movie.steelbook) append(" · Steelbook")
                                    if (movie.slipcover) append(" · Slipcover")
                                    if (movie.limitedEdition) append(" · Limited")
                                    if (movie.isCollection) append(" · Collection")
                                },
                                onClick = { onMovieClick(movie.id) }
                            )
                            if (movie.isCollection && movie.collectionItems.isNotEmpty()) {
                                val itemsExpanded = movie.id in expandedCollections
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedCollections = if (itemsExpanded)
                                                expandedCollections - movie.id
                                            else
                                                expandedCollections + movie.id
                                        }
                                        .padding(start = 80.dp, end = 16.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${movie.collectionItems.size} titles  ${if (itemsExpanded) "▲" else "▼"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                if (itemsExpanded) {
                                    movie.collectionItems.forEach { collectionItem ->
                                        Text(
                                            collectionItem.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(start = 96.dp, end = 16.dp, top = 2.dp, bottom = 2.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

private fun groupLetter(key: String): String {
    val first = key.firstOrNull()?.uppercaseChar() ?: return "#"
    return if (first.isLetter()) first.toString() else "#"
}

private fun titleSortKey(title: String): String {
    val t = title.trim()
    val articles = listOf("The ", "A ", "An ")
    for (article in articles) {
        if (t.startsWith(article, ignoreCase = true)) {
            return t.substring(article.length)
        }
    }
    return t
}
