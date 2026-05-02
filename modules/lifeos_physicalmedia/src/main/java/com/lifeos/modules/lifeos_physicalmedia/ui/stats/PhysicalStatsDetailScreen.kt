package com.lifeos.modules.lifeos_physicalmedia.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_physicalmedia.domain.model.*
import com.lifeos.modules.lifeos_physicalmedia.ui.components.PhysicalItemRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysicalStatsDetailScreen(
    drillDown: PhysicalMediaDrillDown,
    books: List<PhysicalBook>,
    movies: List<PhysicalMovie>,
    games: List<PhysicalGame>,
    tvSeries: List<PhysicalTvSeries>,
    onNavigateBack: () -> Unit
) {
    val filteredItems: List<DrillDownItem> = when (drillDown) {
        is PhysicalMediaDrillDown.BooksByFormat ->
            books.filter { it.format == drillDown.format }
                .sortedBy { titleSortKey(it.title) }
                .map { DrillDownItem(it.title, bookMetadata(it), it.coverLocalPath, it.coverUrl) }
        is PhysicalMediaDrillDown.MoviesByFormat ->
            movies.filter { it.format == drillDown.format }
                .sortedBy { titleSortKey(it.title) }
                .map { DrillDownItem(it.title, movieMetadata(it), it.coverLocalPath, it.coverUrl) }
        is PhysicalMediaDrillDown.MoviesSteelbooks ->
            movies.filter { it.steelbook }
                .sortedBy { titleSortKey(it.title) }
                .map { DrillDownItem(it.title, movieMetadata(it), it.coverLocalPath, it.coverUrl) }
        is PhysicalMediaDrillDown.MoviesLimitedEditions ->
            movies.filter { it.limitedEdition }
                .sortedBy { titleSortKey(it.title) }
                .map { DrillDownItem(it.title, movieMetadata(it), it.coverLocalPath, it.coverUrl) }
        is PhysicalMediaDrillDown.MoviesBoutique ->
            movies.filter { it.boutiqueLabel != null }
                .sortedBy { titleSortKey(it.title) }
                .map { DrillDownItem(it.title, movieMetadata(it), it.coverLocalPath, it.coverUrl) }
        is PhysicalMediaDrillDown.MoviesStandard ->
            movies.filter { it.boutiqueLabel == null }
                .sortedBy { titleSortKey(it.title) }
                .map { DrillDownItem(it.title, movieMetadata(it), it.coverLocalPath, it.coverUrl) }
        is PhysicalMediaDrillDown.MoviesByBoutiqueLabel ->
            movies.filter { it.boutiqueLabel == drillDown.label }
                .sortedBy { titleSortKey(it.title) }
                .map { DrillDownItem(it.title, movieMetadata(it), it.coverLocalPath, it.coverUrl) }
        is PhysicalMediaDrillDown.TvByFormat ->
            tvSeries.filter { it.format == drillDown.format }
                .sortedBy { titleSortKey(it.title) }
                .map { DrillDownItem(it.title, tvMetadata(it), it.coverLocalPath, it.coverUrl) }
        is PhysicalMediaDrillDown.TvCompleteSeries ->
            tvSeries.filter { it.completeSeries }
                .sortedBy { titleSortKey(it.title) }
                .map { DrillDownItem(it.title, tvMetadata(it), it.coverLocalPath, it.coverUrl) }
        is PhysicalMediaDrillDown.GamesBySystem ->
            games.filter { it.system == drillDown.system }
                .sortedBy { titleSortKey(it.title) }
                .map { DrillDownItem(it.title, it.system.displayName(), it.coverLocalPath, it.coverUrl) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(drillDown.title())
                        Text(
                            "${filteredItems.size} item${if (filteredItems.size == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredItems) { item ->
                PhysicalItemRow(
                    coverLocalPath = item.coverLocalPath,
                    coverUrl = item.coverUrl,
                    title = item.title,
                    metadata = item.metadata,
                    onClick = {}
                )
            }
        }
    }
}

private data class DrillDownItem(
    val title: String,
    val metadata: String,
    val coverLocalPath: String?,
    val coverUrl: String?
)

private fun bookMetadata(book: PhysicalBook): String {
    val parts = mutableListOf(book.format.displayName())
    if (book.seriesName != null) parts.add(book.seriesName)
    return parts.joinToString(" · ")
}

private fun movieMetadata(movie: PhysicalMovie): String {
    val parts = mutableListOf(movie.format.displayName())
    if (movie.boutiqueLabel != null) parts.add(movie.boutiqueLabel)
    if (movie.catalogNumber != null) parts.add(movie.catalogNumber)
    val flags = buildList {
        if (movie.steelbook) add("Steelbook")
        if (movie.limitedEdition) add("LE")
        if (movie.slipcover) add("Slipcover")
    }
    if (flags.isNotEmpty()) parts.add(flags.joinToString("/"))
    return parts.joinToString(" · ")
}

private fun tvMetadata(tv: PhysicalTvSeries): String {
    val parts = mutableListOf(tv.format.displayName())
    if (tv.completeSeries) parts.add("Complete")
    if (tv.seriesName != null) parts.add(tv.seriesName)
    return parts.joinToString(" · ")
}

private fun titleSortKey(title: String): String {
    val t = title.trim()
    for (article in listOf("The ", "A ", "An ")) {
        if (t.startsWith(article, ignoreCase = true)) return t.substring(article.length).lowercase()
    }
    return t.lowercase()
}
