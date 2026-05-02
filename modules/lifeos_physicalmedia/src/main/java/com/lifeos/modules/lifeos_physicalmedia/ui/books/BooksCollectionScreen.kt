package com.lifeos.modules.lifeos_physicalmedia.ui.books

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
import com.lifeos.modules.lifeos_physicalmedia.domain.model.PhysicalBook
import com.lifeos.modules.lifeos_physicalmedia.domain.model.displayName
import com.lifeos.modules.lifeos_physicalmedia.ui.components.PhysicalItemRow

enum class BookSortMode { BY_AUTHOR, BY_TITLE, BY_SERIES }

@Composable
fun BooksCollectionScreen(
    books: List<PhysicalBook>,
    onBookClick: (Long) -> Unit,
    onAddBook: () -> Unit,
    onRandomBook: () -> Unit = {},
    listState: LazyListState = rememberLazyListState()
) {
    var sortMode by remember { mutableStateOf(BookSortMode.BY_AUTHOR) }
    var collapsedGroups by remember { mutableStateOf(emptySet<String>()) }
    var searchQuery by remember { mutableStateOf("") }

    val sortedGroups: List<Pair<String, List<PhysicalBook>>> = remember(books, sortMode, searchQuery) {
        val filtered = if (searchQuery.isBlank()) books else books.filter { book ->
            book.title.contains(searchQuery, ignoreCase = true) ||
            book.author.contains(searchQuery, ignoreCase = true) ||
            book.seriesName?.contains(searchQuery, ignoreCase = true) == true
        }
        fun sortWithSeries(list: List<PhysicalBook>): List<PhysicalBook> {
            val nonSeries = list.filter { it.seriesName == null }
            val seriesGroups = list.filter { it.seriesName != null }
                .groupBy { it.seriesName!! }
                .entries.sortedBy { it.key.lowercase() }
                .flatMap { (_, books) -> books.sortedBy { it.seriesNumber ?: Float.MAX_VALUE } }
            return nonSeries + seriesGroups
        }

        when (sortMode) {
            BookSortMode.BY_AUTHOR -> filtered
                .sortedBy { lastName(it.author).lowercase() }
                .groupBy { lastName(it.author).ifBlank { "#" } }
                .entries.sortedWith(compareBy { if (it.key == "#") "zzz" else it.key.lowercase() })
                .map { it.key to sortWithSeries(it.value) }

            BookSortMode.BY_TITLE -> filtered
                .sortedBy { titleSortKey(it.title).lowercase() }
                .groupBy { groupLetter(titleSortKey(it.title)) }
                .entries.sortedWith(compareBy { if (it.key == "#") "zzz" else it.key })
                .map { it.key to sortWithSeries(it.value) }

            BookSortMode.BY_SERIES -> {
                val seriesGroups = filtered
                    .filter { it.seriesName != null }
                    .groupBy { it.seriesName!! }
                    .entries
                    .sortedBy { titleSortKey(it.key).lowercase() }
                    .map { (name, seriesBooks) -> name to seriesBooks.sortedBy { it.seriesNumber ?: Float.MAX_VALUE } }
                val standalone = filtered
                    .filter { it.seriesName == null }
                    .sortedBy { titleSortKey(it.title).lowercase() }
                if (standalone.isEmpty()) seriesGroups
                else seriesGroups + ("Standalone" to standalone)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(onClick = onRandomBook) {
                    Icon(Icons.Default.Casino, "Pick Random Book")
                }
                SmallFloatingActionButton(onClick = onAddBook) {
                    Icon(Icons.Default.Add, "Add Book")
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
                    placeholder = { Text("Search books…") },
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
                            selected = sortMode == BookSortMode.BY_AUTHOR,
                            onClick = { sortMode = BookSortMode.BY_AUTHOR; collapsedGroups = emptySet() },
                            label = { Text("By Author") }
                        )
                        FilterChip(
                            selected = sortMode == BookSortMode.BY_TITLE,
                            onClick = { sortMode = BookSortMode.BY_TITLE; collapsedGroups = emptySet() },
                            label = { Text("By Title") }
                        )
                        FilterChip(
                            selected = sortMode == BookSortMode.BY_SERIES,
                            onClick = { sortMode = BookSortMode.BY_SERIES; collapsedGroups = emptySet() },
                            label = { Text("By Series") }
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

            if (books.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No books yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            sortedGroups.forEach { (groupKey, groupBooks) ->
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
                            "$groupKey  (${groupBooks.size})",
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
                    groupBooks.forEach { book ->
                        item(key = "book_${book.id}") {
                            val seriesMeta = when {
                                sortMode == BookSortMode.BY_SERIES && book.seriesName != null ->
                                    book.seriesNumber?.let { n ->
                                        "#${if (n == n.toLong().toFloat()) n.toLong().toString() else n.toString()} · "
                                    } ?: ""
                                book.seriesName != null -> {
                                    val numStr = book.seriesNumber?.let { "#${if (it == it.toLong().toFloat()) it.toLong().toString() else it.toString()} " } ?: ""
                                    "$numStr${book.seriesName} · "
                                }
                                else -> ""
                            }
                            PhysicalItemRow(
                                coverLocalPath = book.coverLocalPath,
                                coverUrl = book.coverUrl,
                                title = book.title,
                                metadata = "$seriesMeta${book.author} · ${book.format.displayName()}",
                                onClick = { onBookClick(book.id) }
                            )
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

private fun lastName(author: String): String {
    val trimmed = author.trim()
    if (trimmed.isBlank()) return trimmed
    return trimmed.split(" ").last()
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
