package com.lifeos.modules.lifeos_medialogger

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.lifeos.core.LifeOSModule
import com.lifeos.modules.lifeos_medialogger.data.local.MediaType
import com.lifeos.modules.lifeos_medialogger.domain.model.MediaTab
import com.lifeos.modules.lifeos_medialogger.ui.MediaLoggerState
import com.lifeos.modules.lifeos_medialogger.ui.MediaLoggerViewModel
import com.lifeos.modules.lifeos_medialogger.ui.books.AddBookScreen
import com.lifeos.modules.lifeos_medialogger.ui.books.BooksScreen
import com.lifeos.modules.lifeos_medialogger.ui.components.COVER_SEARCH_SUFFIX
import com.lifeos.modules.lifeos_medialogger.ui.components.ImageSearchScreen
import com.lifeos.modules.lifeos_medialogger.ui.components.RatingSelector
import com.lifeos.modules.lifeos_medialogger.ui.games.GamesScreen
import com.lifeos.modules.lifeos_medialogger.ui.movies.MoviesScreen
import java.text.SimpleDateFormat
import java.util.*

class MediaLoggerModule : LifeOSModule {
    override val id: String = "medialogger"
    override val name: String = "Media Logger"
    override val icon: ImageVector = Icons.Filled.VideoLibrary
    override val description: String = "Track books, manga, movies, and games you've consumed"
    override val version: String = "1.0"

    @Composable
    override fun Content(onNavigateBack: () -> Unit, initialId: Long?) {
        val viewModel: MediaLoggerViewModel = hiltViewModel()
        val state by viewModel.state.collectAsState()
        MediaLoggerContent(state = state, viewModel = viewModel, onNavigateBack = onNavigateBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaLoggerContent(
    state: MediaLoggerState,
    viewModel: MediaLoggerViewModel,
    onNavigateBack: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("main") }
    var editItemId by remember { mutableStateOf<Long?>(null) }
    var addVolumeSeriesId by remember { mutableStateOf<Long?>(null) }

    var pendingCoverUrl by remember { mutableStateOf<String?>(null) }
    var pendingTitle by remember { mutableStateOf<String?>(null) }
    var pendingVolumeNumber by remember { mutableStateOf<String?>(null) }
    var imageSearchQuery by remember { mutableStateOf("") }
    var screenBeforeSearch by remember { mutableStateOf("main") }

    fun goToMain() {
        pendingCoverUrl = null
        pendingTitle = null
        pendingVolumeNumber = null
        currentScreen = "main"
    }

    fun goToImageSearch(query: String, returnScreen: String) {
        imageSearchQuery = query
        screenBeforeSearch = returnScreen
        currentScreen = "image_search"
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        if (currentScreen == "main") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
                Text(
                    "Media Logger",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp)
                )
            }

            TabRow(selectedTabIndex = state.selectedTab.ordinal, modifier = Modifier.fillMaxWidth()) {
                MediaTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(when (tab) {
                                MediaTab.BOOKS -> "Books"
                                MediaTab.MOVIES -> "Movies"
                                MediaTab.GAMES -> "Games"
                            })
                        }
                    )
                }
            }

            when (state.selectedTab) {
                MediaTab.BOOKS -> BooksScreen(
                    books = state.books,
                    mangaSeries = state.mangaSeries,
                    expandedSeriesIds = state.expandedSeriesIds,
                    onBookClick = { id -> pendingCoverUrl = null; editItemId = id; currentScreen = "edit_book" },
                    onAddBook = { pendingCoverUrl = null; currentScreen = "add_book" },
                    onAddSeries = { pendingCoverUrl = null; currentScreen = "add_series" },
                    onSeriesClick = { viewModel.toggleSeriesExpanded(it) },
                    onEditSeries = { id -> pendingCoverUrl = null; editItemId = id; currentScreen = "edit_series" },
                    onAddVolume = { seriesId -> addVolumeSeriesId = seriesId; pendingCoverUrl = null; currentScreen = "add_volume" },
                    onEditVolume = { id -> pendingCoverUrl = null; editItemId = id; currentScreen = "edit_volume" }
                )
                MediaTab.MOVIES -> MoviesScreen(
                    movies = state.movies,
                    onMovieClick = { id -> pendingCoverUrl = null; editItemId = id; currentScreen = "edit_movie" },
                    onAddMovie = { pendingCoverUrl = null; currentScreen = "add_movie" }
                )
                MediaTab.GAMES -> GamesScreen(
                    games = state.games,
                    onGameClick = { id -> pendingCoverUrl = null; editItemId = id; currentScreen = "edit_game" },
                    onAddGame = { pendingCoverUrl = null; currentScreen = "add_game" }
                )
            }
        } else {
            when (currentScreen) {
                "image_search" -> ImageSearchScreen(
                    initialQuery = imageSearchQuery,
                    searchService = viewModel.imageSearchService,
                    onImageSelected = { url, query ->
                        pendingCoverUrl = url
                        pendingTitle = query.removeSuffix(COVER_SEARCH_SUFFIX).trim().ifBlank { null }
                        currentScreen = screenBeforeSearch
                    },
                    onNavigateBack = { currentScreen = screenBeforeSearch }
                )

                "add_book" -> AddBookScreen(
                    selectedCoverUrl = pendingCoverUrl,
                    selectedTitle = pendingTitle,
                    onSearchCover = { q -> goToImageSearch(if (q.isBlank()) "cover" else "$q cover", "add_book") },
                    onNavigateBack = { goToMain() },
                    onSave = { title, coverUrl, rating, date, notes, _, author ->
                        viewModel.addMediaItem(title, coverUrl, rating, date, notes, MediaType.BOOK, author = author)
                        goToMain()
                    }
                )

                "edit_book" -> editItemId?.let { id ->
                    val item = viewModel.getBookById(id)
                    AddBookScreen(
                        itemId = id,
                        existingTitle = item?.title ?: "",
                        existingCoverUrl = item?.coverUrl ?: "",
                        existingCoverLocalPath = item?.coverLocalPath ?: "",
                        existingAuthor = item?.author ?: "",
                        existingRating = item?.rating,
                        existingDate = item?.dateCompleted,
                        existingNotes = item?.notes ?: "",
                        selectedCoverUrl = pendingCoverUrl,
                        onSearchCover = { q -> goToImageSearch(q.ifBlank { "book cover" }, "edit_book") },
                        onNavigateBack = { goToMain() },
                        onNavigateBackWithDelete = { viewModel.deleteMediaItem(id); goToMain() },
                        onSave = { title, coverUrl, rating, date, notes, _, author ->
                            viewModel.updateMediaItem(id, title, coverUrl, rating, date, notes, author = author)
                            goToMain()
                        }
                    )
                }

                "add_movie" -> AddBookScreen(
                    isMovie = true,
                    selectedCoverUrl = pendingCoverUrl,
                    selectedTitle = pendingTitle,
                    onSearchCover = { q -> goToImageSearch(if (q.isBlank()) "cover" else "$q cover", "add_movie") },
                    onNavigateBack = { goToMain() },
                    onSave = { title, coverUrl, rating, date, notes, _, _ ->
                        viewModel.addMediaItem(title, coverUrl, rating, date, notes, MediaType.MOVIE)
                        goToMain()
                    }
                )

                "edit_movie" -> editItemId?.let { id ->
                    val item = viewModel.getMovieById(id)
                    AddBookScreen(
                        itemId = id,
                        isMovie = true,
                        existingTitle = item?.title ?: "",
                        existingCoverUrl = item?.coverUrl ?: "",
                        existingCoverLocalPath = item?.coverLocalPath ?: "",
                        existingRating = item?.rating,
                        existingDate = item?.dateCompleted,
                        existingNotes = item?.notes ?: "",
                        selectedCoverUrl = pendingCoverUrl,
                        onSearchCover = { q -> goToImageSearch(q.ifBlank { "movie poster" }, "edit_movie") },
                        onNavigateBack = { goToMain() },
                        onNavigateBackWithDelete = { viewModel.deleteMediaItem(id); goToMain() },
                        onSave = { title, coverUrl, rating, date, notes, _, _ ->
                            viewModel.updateMediaItem(id, title, coverUrl, rating, date, notes)
                            goToMain()
                        }
                    )
                }

                "add_game" -> AddBookScreen(
                    isGame = true,
                    selectedCoverUrl = pendingCoverUrl,
                    selectedTitle = pendingTitle,
                    onSearchCover = { q -> goToImageSearch(if (q.isBlank()) "cover" else "$q cover", "add_game") },
                    onNavigateBack = { goToMain() },
                    onSave = { title, coverUrl, rating, date, notes, platform, _ ->
                        viewModel.addMediaItem(title, coverUrl, rating, date, notes, MediaType.GAME, platform = platform)
                        goToMain()
                    }
                )

                "edit_game" -> editItemId?.let { id ->
                    val item = viewModel.getGameById(id)
                    AddBookScreen(
                        itemId = id,
                        isGame = true,
                        existingTitle = item?.title ?: "",
                        existingCoverUrl = item?.coverUrl ?: "",
                        existingCoverLocalPath = item?.coverLocalPath ?: "",
                        existingRating = item?.rating,
                        existingDate = item?.dateCompleted,
                        existingNotes = item?.notes ?: "",
                        existingPlatform = item?.platform ?: "",
                        selectedCoverUrl = pendingCoverUrl,
                        onSearchCover = { q -> goToImageSearch(q.ifBlank { "game cover art" }, "edit_game") },
                        onNavigateBack = { goToMain() },
                        onNavigateBackWithDelete = { viewModel.deleteMediaItem(id); goToMain() },
                        onSave = { title, coverUrl, rating, date, notes, platform, _ ->
                            viewModel.updateMediaItem(id, title, coverUrl, rating, date, notes, platform = platform)
                            goToMain()
                        }
                    )
                }

                "add_series" -> AddBookScreen(
                    isSeries = true,
                    selectedCoverUrl = pendingCoverUrl,
                    selectedTitle = pendingTitle,
                    onSearchCover = { q -> goToImageSearch(if (q.isBlank()) "cover" else "$q cover", "add_series") },
                    onNavigateBack = { goToMain() },
                    onSave = { title, coverUrl, _, date, _, _, author ->
                        viewModel.addMangaSeries(title, coverUrl, author, date)
                        goToMain()
                    }
                )

                "edit_series" -> editItemId?.let { id ->
                    val series = viewModel.getSeriesById(id)
                    AddBookScreen(
                        itemId = id,
                        isSeries = true,
                        existingTitle = series?.title ?: "",
                        existingCoverUrl = series?.coverUrl ?: "",
                        existingCoverLocalPath = series?.coverLocalPath ?: "",
                        existingAuthor = series?.author ?: "",
                        existingDate = series?.dateCompleted,
                        selectedCoverUrl = pendingCoverUrl,
                        onSearchCover = { q -> goToImageSearch(q.ifBlank { "manga cover" }, "edit_series") },
                        onNavigateBack = { goToMain() },
                        onNavigateBackWithDelete = { viewModel.deleteMangaSeries(id); goToMain() },
                        onSave = { title, coverUrl, _, date, _, _, author ->
                            viewModel.updateMangaSeries(id, title, coverUrl, author, date)
                            goToMain()
                        }
                    )
                }

                "add_volume" -> addVolumeSeriesId?.let { seriesId ->
                    val series = state.mangaSeries.find { it.id == seriesId }
                    AddVolumeScreen(
                        seriesName = series?.title ?: "",
                        selectedCoverUrl = pendingCoverUrl,
                        selectedVolumeNumber = pendingVolumeNumber,
                        onSearchCover = { q ->
                            // Query is "$seriesName vol $volNum" — capture the number so it
                            // survives the navigation to image search and back
                            pendingVolumeNumber = q.substringAfterLast(" vol ")
                                .trim()
                                .takeIf { it.isNotEmpty() && it.all(Char::isDigit) }
                            goToImageSearch(q.ifBlank { "manga volume cover" }, "add_volume")
                        },
                        onNavigateBack = { goToMain() },
                        onSave = { volumeNumber, rating, date, coverUrl, notes ->
                            viewModel.addMangaVolume(seriesId, volumeNumber, rating, date, coverUrl, notes)
                            goToMain()
                        }
                    )
                }

                "edit_volume" -> editItemId?.let { id ->
                    val volume = viewModel.getVolumeById(id)
                    val series = state.mangaSeries.find { it.id == volume?.seriesId }
                    AddVolumeScreen(
                        volumeId = id,
                        seriesName = series?.title ?: "",
                        existingVolumeNumber = volume?.volumeNumber?.toString() ?: "",
                        existingRating = volume?.rating,
                        existingDate = volume?.dateCompleted,
                        existingCoverUrl = volume?.coverUrl ?: "",
                        existingCoverLocalPath = volume?.coverLocalPath ?: "",
                        existingNotes = volume?.notes ?: "",
                        selectedCoverUrl = pendingCoverUrl,
                        onSearchCover = { q -> goToImageSearch(q.ifBlank { "manga volume cover" }, "edit_volume") },
                        onNavigateBack = { goToMain() },
                        onNavigateBackWithDelete = { viewModel.deleteMangaVolume(id); goToMain() },
                        onSave = { volumeNumber, rating, date, coverUrl, notes ->
                            viewModel.updateMangaVolume(id, volumeNumber, rating, date, coverUrl, notes)
                            goToMain()
                        }
                    )
                }
            }
        }

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddVolumeScreen(
    volumeId: Long? = null,
    seriesName: String,
    existingVolumeNumber: String = "",
    existingRating: Float? = null,
    existingDate: Long? = null,
    existingCoverUrl: String = "",
    existingCoverLocalPath: String = "",
    existingNotes: String = "",
    selectedCoverUrl: String? = null,
    selectedVolumeNumber: String? = null,
    onSearchCover: ((String) -> Unit)? = null,
    onNavigateBack: () -> Unit,
    onNavigateBackWithDelete: (() -> Unit)? = null,
    onSave: (volumeNumber: Int, rating: Float?, dateCompleted: Long?, coverUrl: String?, notes: String?) -> Unit
) {
    var volumeNumber by remember { mutableStateOf(existingVolumeNumber) }
    var selectedRating by remember { mutableStateOf<Float?>(existingRating) }
    var selectedDate by remember { mutableStateOf(existingDate) }
    var coverUrl by remember { mutableStateOf(existingCoverUrl) }
    var coverLocalPath by remember { mutableStateOf(existingCoverLocalPath) }
    var notes by remember { mutableStateOf(existingNotes) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCoverUrl) {
        if (selectedCoverUrl != null) {
            coverUrl = selectedCoverUrl
            coverLocalPath = ""
        }
    }

    LaunchedEffect(selectedVolumeNumber) {
        if (selectedVolumeNumber != null) {
            volumeNumber = selectedVolumeNumber
        }
    }

    val coverModel = coverLocalPath.takeIf { it.isNotBlank() } ?: coverUrl.takeIf { it.isNotBlank() }
    val screenTitle = if (volumeId != null) "Edit Volume" else "Add Volume"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
                Text(
                    "$screenTitle – $seriesName",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            if (onNavigateBackWithDelete != null) {
                IconButton(onClick = onNavigateBackWithDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = volumeNumber,
            onValueChange = { volumeNumber = it.filter { c -> c.isDigit() } },
            label = { Text("Volume Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (coverModel != null) {
            AsyncImage(
                model = coverModel,
                contentDescription = "Cover preview",
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (onSearchCover != null) {
            val volNum = volumeNumber.ifBlank { "1" }
            OutlinedButton(
                onClick = { onSearchCover("$seriesName vol $volNum") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (coverModel == null) "Search for cover" else "Change cover")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Rating", style = MaterialTheme.typography.labelLarge)
        RatingSelector(
            selectedRating = selectedRating,
            onRatingSelected = { selectedRating = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = selectedDate?.let {
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it))
            } ?: "",
            onValueChange = {},
            label = { Text("Date Completed") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                TextButton(onClick = { showDatePicker = true }) { Text("Pick") }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                volumeNumber.toIntOrNull()?.let { num ->
                    onSave(num, selectedRating, selectedDate, coverUrl.ifBlank { null }, notes.ifBlank { null })
                }
            },
            enabled = volumeNumber.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis?.let { it + (24 * 60 * 60 * 1000) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
