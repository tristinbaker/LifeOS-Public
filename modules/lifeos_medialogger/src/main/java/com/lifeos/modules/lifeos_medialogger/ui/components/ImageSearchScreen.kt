package com.lifeos.modules.lifeos_medialogger.ui.components

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
import com.lifeos.modules.lifeos_medialogger.service.ImageSearchResult
import com.lifeos.modules.lifeos_medialogger.service.ImageSearchService
import kotlinx.coroutines.launch

@Composable
fun ImageSearchScreen(
    initialQuery: String,
    searchService: ImageSearchService,
    onImageSelected: (url: String, query: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var query by remember { mutableStateOf(initialQuery) }
    var results by remember { mutableStateOf<List<ImageSearchResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun doSearch() {
        if (query.isBlank()) return
        scope.launch {
            isLoading = true
            message = null
            results = searchService.search(query)
            isLoading = false
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

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (message != null && !isLoading) {
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
                        .clickable { onImageSelected(result.imageUrl, query) },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

// exposed so callers can strip it back out
const val COVER_SEARCH_SUFFIX = " cover"
