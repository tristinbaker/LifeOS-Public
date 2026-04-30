package com.lifeos.modules.lifeos_physicalmedia

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeos.core.LifeOSModule
import com.lifeos.modules.lifeos_physicalmedia.data.local.BookFormat
import com.lifeos.modules.lifeos_physicalmedia.data.local.GameSystem
import com.lifeos.modules.lifeos_physicalmedia.data.local.MovieFormat
import com.lifeos.modules.lifeos_physicalmedia.domain.model.PhysicalMediaTab
import com.lifeos.modules.lifeos_physicalmedia.ui.PhysicalMediaState
import com.lifeos.modules.lifeos_physicalmedia.ui.PhysicalMediaViewModel
import com.lifeos.modules.lifeos_physicalmedia.ui.books.AddEditBookScreen
import com.lifeos.modules.lifeos_physicalmedia.ui.books.BooksCollectionScreen
import com.lifeos.modules.lifeos_physicalmedia.ui.components.ImageSearchScreen
import com.lifeos.modules.lifeos_physicalmedia.ui.games.AddEditGameScreen
import com.lifeos.modules.lifeos_physicalmedia.ui.games.GamesCollectionScreen
import com.lifeos.modules.lifeos_physicalmedia.ui.movies.AddEditMovieScreen
import com.lifeos.modules.lifeos_physicalmedia.ui.movies.MoviesCollectionScreen
import com.lifeos.modules.lifeos_physicalmedia.ui.stats.PhysicalStatsDetailScreen
import com.lifeos.modules.lifeos_physicalmedia.ui.stats.PhysicalStatsScreen
import com.lifeos.modules.lifeos_physicalmedia.ui.tvseries.AddEditTvSeriesScreen
import com.lifeos.modules.lifeos_physicalmedia.ui.tvseries.TvSeriesCollectionScreen

class PhysicalMediaModule : LifeOSModule {
    override val id: String = "physicalmedia"
    override val name: String = "Collection Tracker"
    override val icon: ImageVector = Icons.Filled.Inventory2
    override val description: String = "Track your physical book, movie, and game collection"
    override val shortDescription: String = "Physical collection"
    override val version: String = "1.0"

    @Composable
    override fun Content(onNavigateBack: () -> Unit, initialId: Long?) {
        val viewModel: PhysicalMediaViewModel = hiltViewModel()
        val state by viewModel.state.collectAsState()
        PhysicalMediaContent(state = state, viewModel = viewModel, onNavigateBack = onNavigateBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PhysicalMediaContent(
    state: PhysicalMediaState,
    viewModel: PhysicalMediaViewModel,
    onNavigateBack: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("main") }
    var editItemId by remember { mutableStateOf<Long?>(null) }
    var pendingCoverLocalPath by remember { mutableStateOf<String?>(null) }
    var imageSearchQuery by remember { mutableStateOf("") }
    var screenBeforeSearch by remember { mutableStateOf("main") }

    val booksListState = rememberLazyListState()
    val moviesListState = rememberLazyListState()
    val gamesListState = rememberLazyListState()

    fun goToMain() {
        pendingCoverLocalPath?.let { viewModel.discardPendingCover(it) }
        pendingCoverLocalPath = null
        currentScreen = "main"
    }

    fun goToMainAfterSave() {
        pendingCoverLocalPath = null
        currentScreen = "main"
    }

    fun goToImageSearch(query: String, returnScreen: String) {
        imageSearchQuery = query
        screenBeforeSearch = returnScreen
        currentScreen = "image_search"
    }

    val effectiveScreen = if (currentScreen == "image_search") screenBeforeSearch else currentScreen

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        when (effectiveScreen) {
            "main" -> Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                    Text(
                        "Collection Tracker",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp, top = 12.dp)
                    )
                }

                val pagerState = rememberPagerState(
                    initialPage = state.selectedTab.ordinal,
                    pageCount = { PhysicalMediaTab.entries.size }
                )

                LaunchedEffect(state.selectedTab) {
                    if (pagerState.currentPage != state.selectedTab.ordinal) {
                        pagerState.animateScrollToPage(state.selectedTab.ordinal)
                    }
                }

                LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
                    if (!pagerState.isScrollInProgress) {
                        viewModel.selectTab(PhysicalMediaTab.entries[pagerState.currentPage])
                    }
                }

                TabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.fillMaxWidth()) {
                    PhysicalMediaTab.entries.forEach { tab ->
                        Tab(
                            selected = pagerState.currentPage == tab.ordinal,
                            onClick = { viewModel.selectTab(tab) },
                            text = {
                                Text(
                                    when (tab) {
                                        PhysicalMediaTab.BOOKS -> "Books"
                                        PhysicalMediaTab.MOVIES -> "Movies"
                                        PhysicalMediaTab.GAMES -> "Games"
                                        PhysicalMediaTab.TV_SERIES -> "TV"
                                        PhysicalMediaTab.STATS -> "Stats"
                                    }
                                )
                            }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (PhysicalMediaTab.entries[page]) {
                        PhysicalMediaTab.BOOKS -> BooksCollectionScreen(
                            books = state.books,
                            onBookClick = { id -> pendingCoverLocalPath = null; editItemId = id; currentScreen = "edit_book" },
                            onAddBook = { pendingCoverLocalPath = null; currentScreen = "add_book" },
                            listState = booksListState
                        )
                        PhysicalMediaTab.MOVIES -> MoviesCollectionScreen(
                            movies = state.movies,
                            onMovieClick = { id -> pendingCoverLocalPath = null; editItemId = id; currentScreen = "edit_movie" },
                            onAddMovie = { pendingCoverLocalPath = null; currentScreen = "add_movie" },
                            listState = moviesListState
                        )
                        PhysicalMediaTab.GAMES -> GamesCollectionScreen(
                            games = state.games,
                            onGameClick = { id -> pendingCoverLocalPath = null; editItemId = id; currentScreen = "edit_game" },
                            onAddGame = { pendingCoverLocalPath = null; currentScreen = "add_game" },
                            listState = gamesListState
                        )
                        PhysicalMediaTab.TV_SERIES -> TvSeriesCollectionScreen(
                            tvSeries = state.tvSeries,
                            onAdd = { pendingCoverLocalPath = null; currentScreen = "add_tv" },
                            onEdit = { id -> pendingCoverLocalPath = null; editItemId = id; currentScreen = "edit_tv" }
                        )
                        PhysicalMediaTab.STATS -> PhysicalStatsScreen(
                            stats = state.stats,
                            gamePrices = state.gamePrices,
                            isFetchingPrices = state.isFetchingPrices,
                            pricesFetchCount = state.pricesFetchCount,
                            totalGames = state.games.size,
                            onFetchPrices = { viewModel.fetchGamePrices() },
                            onDrillDown = { filter ->
                                viewModel.setDrillDown(filter)
                                currentScreen = "stats_detail"
                            }
                        )
                    }
                }
            }

            "add_book" -> AddEditBookScreen(
                knownAuthors = state.distinctBookAuthors,
                knownSeriesNames = state.distinctBookSeriesNames,
                seriesNumbers = state.bookSeriesNumbers,
                selectedCoverLocalPath = pendingCoverLocalPath,
                onSearchCover = { query -> goToImageSearch(query, "add_book") },
                onNavigateBack = { goToMain() },
                onSave = { title, author, format, coverUrl, coverLocalPath, seriesName, seriesNumber ->
                    viewModel.addBook(title, author, format, coverUrl, coverLocalPath, seriesName, seriesNumber)
                    goToMainAfterSave()
                }
            )

            "edit_book" -> {
                val book = editItemId?.let { viewModel.getBookById(it) }
                AddEditBookScreen(
                    itemId = editItemId,
                    existingTitle = book?.title ?: "",
                    existingAuthor = book?.author ?: "",
                    existingFormat = book?.format ?: BookFormat.HARDCOVER,
                    existingCoverUrl = book?.coverUrl ?: "",
                    existingCoverLocalPath = book?.coverLocalPath ?: "",
                    existingSeriesName = book?.seriesName ?: "",
                    existingSeriesNumber = book?.seriesNumber?.toString() ?: "",
                    knownAuthors = state.distinctBookAuthors,
                    knownSeriesNames = state.distinctBookSeriesNames,
                    selectedCoverLocalPath = pendingCoverLocalPath,
                    onSearchCover = { query -> goToImageSearch(query, "edit_book") },
                    onNavigateBack = { goToMain() },
                    onNavigateBackWithDelete = {
                        editItemId?.let { viewModel.deleteBook(it) }
                        goToMain()
                    },
                    onSave = { title, author, format, coverUrl, coverLocalPath, seriesName, seriesNumber ->
                        editItemId?.let { viewModel.updateBook(it, title, author, format, coverUrl, coverLocalPath, seriesName, seriesNumber) }
                        goToMainAfterSave()
                    }
                )
            }

            "add_movie" -> AddEditMovieScreen(
                existingFormat = state.lastMovieFormat,
                knownBoutiqueLabels = state.distinctMovieBoutiqueLabels,
                selectedCoverLocalPath = pendingCoverLocalPath,
                onSearchCover = { query -> goToImageSearch(query, "add_movie") },
                onNavigateBack = { goToMain() },
                onSave = { title, format, limited, steelbook, slipcover, boutiqueLabel, catalogNumber, coverUrl, coverLocalPath ->
                    viewModel.addMovie(title, format, limited, steelbook, slipcover, boutiqueLabel, catalogNumber, coverUrl, coverLocalPath)
                    goToMainAfterSave()
                }
            )

            "edit_movie" -> {
                val movie = editItemId?.let { viewModel.getMovieById(it) }
                AddEditMovieScreen(
                    itemId = editItemId,
                    existingTitle = movie?.title ?: "",
                    existingFormat = movie?.format ?: MovieFormat.BLU_RAY,
                    existingLimitedEdition = movie?.limitedEdition ?: false,
                    existingSteelbook = movie?.steelbook ?: false,
                    existingSlipcover = movie?.slipcover ?: false,
                    existingBoutiqueLabel = movie?.boutiqueLabel ?: "",
                    existingCatalogNumber = movie?.catalogNumber ?: "",
                    existingCoverUrl = movie?.coverUrl ?: "",
                    existingCoverLocalPath = movie?.coverLocalPath ?: "",
                    knownBoutiqueLabels = state.distinctMovieBoutiqueLabels,
                    selectedCoverLocalPath = pendingCoverLocalPath,
                    onSearchCover = { query -> goToImageSearch(query, "edit_movie") },
                    onNavigateBack = { goToMain() },
                    onNavigateBackWithDelete = {
                        editItemId?.let { viewModel.deleteMovie(it) }
                        goToMain()
                    },
                    onSave = { title, format, limited, steelbook, slipcover, boutiqueLabel, catalogNumber, coverUrl, coverLocalPath ->
                        editItemId?.let { viewModel.updateMovie(it, title, format, limited, steelbook, slipcover, boutiqueLabel, catalogNumber, coverUrl, coverLocalPath) }
                        goToMainAfterSave()
                    }
                )
            }

            "add_game" -> AddEditGameScreen(
                existingSystem = state.lastGameSystem,
                selectedCoverLocalPath = pendingCoverLocalPath,
                onSearchCover = { query -> goToImageSearch(query, "add_game") },
                onNavigateBack = { goToMain() },
                onSave = { title, system, coverUrl, coverLocalPath ->
                    viewModel.addGame(title, system, coverUrl, coverLocalPath)
                    goToMainAfterSave()
                }
            )

            "edit_game" -> {
                val game = editItemId?.let { viewModel.getGameById(it) }
                AddEditGameScreen(
                    itemId = editItemId,
                    existingTitle = game?.title ?: "",
                    existingSystem = game?.system ?: GameSystem.SWITCH,
                    existingCoverUrl = game?.coverUrl ?: "",
                    existingCoverLocalPath = game?.coverLocalPath ?: "",
                    selectedCoverLocalPath = pendingCoverLocalPath,
                    onSearchCover = { query -> goToImageSearch(query, "edit_game") },
                    onNavigateBack = { goToMain() },
                    onNavigateBackWithDelete = {
                        editItemId?.let { viewModel.deleteGame(it) }
                        goToMain()
                    },
                    onSave = { title, system, coverUrl, coverLocalPath ->
                        editItemId?.let { viewModel.updateGame(it, title, system, coverUrl, coverLocalPath) }
                        goToMainAfterSave()
                    }
                )
            }

            "add_tv" -> AddEditTvSeriesScreen(
                knownSeriesNames = state.distinctTvSeriesNames,
                seriesNumbers = state.tvSeriesNumbers,
                selectedCoverLocalPath = pendingCoverLocalPath,
                onSearchCover = { query -> goToImageSearch(query, "add_tv") },
                onNavigateBack = { goToMain() },
                onSave = { title, format, completeSeries, seriesName, seriesNumber, coverUrl, coverLocalPath ->
                    viewModel.addTvSeries(title, format, completeSeries, seriesName, seriesNumber, coverUrl, coverLocalPath)
                    goToMainAfterSave()
                }
            )

            "stats_detail" -> {
                val drillDown = state.drillDown
                if (drillDown != null) {
                    PhysicalStatsDetailScreen(
                        drillDown = drillDown,
                        books = state.books,
                        movies = state.movies,
                        games = state.games,
                        tvSeries = state.tvSeries,
                        onNavigateBack = {
                            viewModel.setDrillDown(null)
                            currentScreen = "main"
                        }
                    )
                } else {
                    currentScreen = "main"
                }
            }

            "edit_tv" -> {
                val show = editItemId?.let { viewModel.getTvSeriesById(it) }
                AddEditTvSeriesScreen(
                    itemId = editItemId,
                    existingTitle = show?.title ?: "",
                    existingFormat = show?.format ?: MovieFormat.BLU_RAY,
                    existingCompleteSeries = show?.completeSeries ?: false,
                    existingSeriesName = show?.seriesName ?: "",
                    existingSeriesNumber = show?.seriesNumber?.toString() ?: "",
                    knownSeriesNames = state.distinctTvSeriesNames,
                    seriesNumbers = state.tvSeriesNumbers,
                    selectedCoverLocalPath = pendingCoverLocalPath,
                    onSearchCover = { query -> goToImageSearch(query, "edit_tv") },
                    onNavigateBack = { goToMain() },
                    onNavigateBackWithDelete = {
                        editItemId?.let { viewModel.deleteTvSeries(it) }
                        goToMain()
                    },
                    onSave = { title, format, completeSeries, seriesName, seriesNumber, coverUrl, coverLocalPath ->
                        editItemId?.let { viewModel.updateTvSeries(it, title, format, completeSeries, seriesName, seriesNumber, coverUrl, coverLocalPath) }
                        goToMainAfterSave()
                    }
                )
            }
        }

        if (currentScreen == "image_search") {
            Surface(modifier = Modifier.fillMaxSize()) {
                ImageSearchScreen(
                    initialQuery = imageSearchQuery,
                    searchService = viewModel.imageSearchService,
                    imageCacheService = viewModel.imageCacheService,
                    onImageSelected = { localPath, _ ->
                        pendingCoverLocalPath?.let { viewModel.discardPendingCover(it) }
                        pendingCoverLocalPath = localPath
                        currentScreen = screenBeforeSearch
                    },
                    onNavigateBack = { currentScreen = screenBeforeSearch }
                )
            }
        }
    }
}
