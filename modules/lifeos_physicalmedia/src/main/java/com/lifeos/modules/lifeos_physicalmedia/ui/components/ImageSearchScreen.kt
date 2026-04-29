package com.lifeos.modules.lifeos_physicalmedia.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lifeos.modules.lifeos_physicalmedia.service.ImageCacheService
import com.lifeos.modules.lifeos_physicalmedia.service.ImageSearchResult
import com.lifeos.modules.lifeos_physicalmedia.service.ImageSearchService
import kotlinx.coroutines.launch

@Composable
fun ImageSearchScreen(
    initialQuery: String,
    searchService: ImageSearchService,
    imageCacheService: ImageCacheService,
    onImageSelected: (localPath: String, query: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var query by remember { mutableStateOf(initialQuery) }
    var results by remember { mutableStateOf<List<ImageSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun doSearch() {
        if (query.isBlank()) return
        scope.launch {
            isSearching = true
            message = null
            results = searchService.search(query)
            isSearching = false
            if (results.isEmpty()) message = "No results found"
        }
    }

    LaunchedEffect(Unit) {
        if (initialQuery.isNotBlank()) doSearch()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search images...") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { doSearch() }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                }
            )
        }

        if (isSearching || isDownloading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (isDownloading) {
            Text(
                "Downloading image…",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (message != null && !isSearching && !isDownloading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(message!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(results, key = { it.imageUrl }) { result ->
                AsyncImage(
                    model = result.thumbnail,
                    contentDescription = result.title,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable(enabled = !isDownloading && !isSearching) {
                            scope.launch {
                                isDownloading = true
                                message = null
                                val localPath = imageCacheService.downloadAndCacheImage(result.imageUrl)
                                isDownloading = false
                                if (localPath != null) {
                                    onImageSelected(localPath, query)
                                } else {
                                    message = "Could not download that image — try another"
                                }
                            }
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
