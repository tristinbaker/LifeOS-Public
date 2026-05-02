package com.lifeos.modules.lifeos_physicalmedia.ui.games

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
import com.lifeos.modules.lifeos_physicalmedia.domain.model.PhysicalGame
import com.lifeos.modules.lifeos_physicalmedia.domain.model.displayName
import com.lifeos.modules.lifeos_physicalmedia.ui.components.PhysicalItemRow

enum class GameSortMode { BY_SYSTEM, BY_TITLE }

@Composable
fun GamesCollectionScreen(
    games: List<PhysicalGame>,
    onGameClick: (Long) -> Unit,
    onAddGame: () -> Unit,
    onRandomGame: () -> Unit = {},
    listState: LazyListState = rememberLazyListState()
) {
    var sortMode by remember { mutableStateOf(GameSortMode.BY_SYSTEM) }
    var collapsedGroups by remember { mutableStateOf(emptySet<String>()) }
    var expandedCollections by remember { mutableStateOf(emptySet<Long>()) }
    var searchQuery by remember { mutableStateOf("") }

    val sortedGroups: List<Pair<String, List<PhysicalGame>>> = remember(games, sortMode, searchQuery) {
        val filtered = if (searchQuery.isBlank()) games else games.filter { game ->
            game.title.contains(searchQuery, ignoreCase = true) ||
            game.system.displayName().contains(searchQuery, ignoreCase = true)
        }
        when (sortMode) {
            GameSortMode.BY_SYSTEM -> filtered
                .sortedBy { titleSortKey(it.title).lowercase() }
                .groupBy { it.system.displayName() }
                .entries.sortedBy { it.key }
                .map { it.key to it.value }

            GameSortMode.BY_TITLE -> filtered
                .sortedBy { titleSortKey(it.title).lowercase() }
                .groupBy { groupLetter(titleSortKey(it.title)) }
                .entries.sortedWith(compareBy { if (it.key == "#") "zzz" else it.key })
                .map { it.key to it.value }
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(onClick = onRandomGame) {
                    Icon(Icons.Default.Casino, "Pick Random Game")
                }
                SmallFloatingActionButton(onClick = onAddGame) {
                    Icon(Icons.Default.Add, "Add Game")
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
                    placeholder = { Text("Search games…") },
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
                            selected = sortMode == GameSortMode.BY_SYSTEM,
                            onClick = { sortMode = GameSortMode.BY_SYSTEM; collapsedGroups = emptySet() },
                            label = { Text("By System") }
                        )
                        FilterChip(
                            selected = sortMode == GameSortMode.BY_TITLE,
                            onClick = { sortMode = GameSortMode.BY_TITLE; collapsedGroups = emptySet() },
                            label = { Text("By Title") }
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

            if (games.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No games yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            sortedGroups.forEach { (groupKey, groupGames) ->
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
                            "$groupKey  (${groupGames.size})",
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
                    groupGames.forEach { game ->
                        item(key = "game_${game.id}") {
                            PhysicalItemRow(
                                coverLocalPath = game.coverLocalPath,
                                coverUrl = game.coverUrl,
                                title = game.title,
                                metadata = if (game.isCollection) "${game.system.displayName()} · Collection" else game.system.displayName(),
                                onClick = { onGameClick(game.id) }
                            )
                            if (game.isCollection && game.collectionItems.isNotEmpty()) {
                                val itemsExpanded = game.id in expandedCollections
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedCollections = if (itemsExpanded)
                                                expandedCollections - game.id
                                            else
                                                expandedCollections + game.id
                                        }
                                        .padding(start = 80.dp, end = 16.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${game.collectionItems.size} titles  ${if (itemsExpanded) "▲" else "▼"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                if (itemsExpanded) {
                                    game.collectionItems.forEach { collectionItem ->
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
