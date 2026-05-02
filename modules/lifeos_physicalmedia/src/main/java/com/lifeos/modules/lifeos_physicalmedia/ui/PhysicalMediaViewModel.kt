package com.lifeos.modules.lifeos_physicalmedia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_medialogger.data.repository.MediaLoggerRepository
import com.lifeos.modules.lifeos_physicalmedia.data.local.*
import com.lifeos.modules.lifeos_physicalmedia.data.preferences.PhysicalMediaPreferences
import com.lifeos.modules.lifeos_physicalmedia.data.repository.PhysicalMediaRepository
import com.lifeos.modules.lifeos_physicalmedia.domain.model.*
import com.lifeos.modules.lifeos_physicalmedia.service.ImageCacheService
import com.lifeos.modules.lifeos_physicalmedia.service.ImageSearchService
import com.lifeos.modules.lifeos_physicalmedia.service.PriceChartingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class PhysicalMediaState(
    val selectedTab: PhysicalMediaTab = PhysicalMediaTab.BOOKS,
    val books: List<PhysicalBook> = emptyList(),
    val movies: List<PhysicalMovie> = emptyList(),
    val games: List<PhysicalGame> = emptyList(),
    val tvSeries: List<PhysicalTvSeries> = emptyList(),
    val stats: PhysicalMediaStats = PhysicalMediaStats(),
    val lastGameSystem: GameSystem = GameSystem.SWITCH,
    val lastMovieFormat: MovieFormat = MovieFormat.BLU_RAY,
    val isLoading: Boolean = true,
    val gamePrices: Map<Long, Double?> = emptyMap(),
    val isFetchingPrices: Boolean = false,
    val pricesFetchCount: Int = 0,
    val drillDown: PhysicalMediaDrillDown? = null,
    val randomBook: PhysicalBook? = null,
    val randomMovie: PhysicalMovie? = null,
    val randomMovieItemTitle: String? = null,
    val randomGame: PhysicalGame? = null,
    val randomGameItemTitle: String? = null,
    val randomExcludedCount: Int = 0
) {
    val distinctBookAuthors: List<String>
        get() = books.map { it.author }.filter { it.isNotBlank() }.distinct().sorted()
    val distinctBookSeriesNames: List<String>
        get() = books.mapNotNull { it.seriesName }.filter { it.isNotBlank() }.distinct().sorted()
    val bookSeriesNumbers: Map<String, List<Float>>
        get() = books.filter { it.seriesName != null && it.seriesNumber != null }
            .groupBy { it.seriesName!! }
            .mapValues { (_, bs) -> bs.mapNotNull { it.seriesNumber }.sorted() }
    val distinctTvSeriesNames: List<String>
        get() = tvSeries.mapNotNull { it.seriesName }.filter { it.isNotBlank() }.distinct().sorted()
    val tvSeriesNumbers: Map<String, List<Float>>
        get() = tvSeries.filter { it.seriesName != null && it.seriesNumber != null }
            .groupBy { it.seriesName!! }
            .mapValues { (_, ts) -> ts.mapNotNull { it.seriesNumber }.sorted() }
    val distinctMovieBoutiqueLabels: List<String>
        get() = movies.mapNotNull { it.boutiqueLabel }.filter { it.isNotBlank() }.distinct().sorted()
}

@HiltViewModel
class PhysicalMediaViewModel @Inject constructor(
    private val repository: PhysicalMediaRepository,
    private val preferences: PhysicalMediaPreferences,
    val imageCacheService: ImageCacheService,
    val imageSearchService: ImageSearchService,
    private val priceChartingService: PriceChartingService,
    private val mediaLoggerRepository: MediaLoggerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PhysicalMediaState())
    val state: StateFlow<PhysicalMediaState> = _state.asStateFlow()

    private var currentBooks: List<PhysicalBook> = emptyList()
    private var currentMovies: List<PhysicalMovie> = emptyList()
    private var currentGames: List<PhysicalGame> = emptyList()
    private var currentTvSeries: List<PhysicalTvSeries> = emptyList()

    private val _recentBookTitles = MutableStateFlow<Set<String>>(emptySet())
    private val _recentMovieTitles = MutableStateFlow<Set<String>>(emptySet())
    private val _recentGameTitles = MutableStateFlow<Set<String>>(emptySet())

    init {
        viewModelScope.launch {
            repository.getBooks().collect { entities ->
                currentBooks = entities.map { it.toDomain() }
                _state.update { it.copy(books = currentBooks, stats = computeStats(), isLoading = false) }
            }
        }
        viewModelScope.launch {
            combine(repository.getMovies(), repository.getMovieCollectionItems()) { movies, items ->
                val byMovie = items.groupBy { it.movieId }
                movies.map { it.toDomain(byMovie[it.id] ?: emptyList()) }
            }.collect { movies ->
                currentMovies = movies
                _state.update { it.copy(movies = currentMovies, stats = computeStats()) }
            }
        }
        viewModelScope.launch {
            combine(repository.getGames(), repository.getGameCollectionItems()) { games, items ->
                val byGame = items.groupBy { it.gameId }
                games.map { it.toDomain(byGame[it.id] ?: emptyList()) }
            }.collect { games ->
                currentGames = games
                _state.update { it.copy(games = currentGames, stats = computeStats()) }
            }
        }
        viewModelScope.launch {
            repository.getTvSeries().collect { entities ->
                currentTvSeries = entities.map { it.toDomain() }
                _state.update { it.copy(tvSeries = currentTvSeries, stats = computeStats()) }
            }
        }
        viewModelScope.launch {
            preferences.lastGameSystem.collect { system ->
                _state.update { it.copy(lastGameSystem = system) }
            }
        }
        viewModelScope.launch {
            preferences.lastMovieFormat.collect { format ->
                _state.update { it.copy(lastMovieFormat = format) }
            }
        }
        val oneYearMs = 365L * 24 * 60 * 60 * 1000
        viewModelScope.launch {
            mediaLoggerRepository.getBooks().collect { items ->
                val cutoff = System.currentTimeMillis() - oneYearMs
                _recentBookTitles.value = items.filter { (it.dateCompleted ?: 0L) >= cutoff }.map { it.title }.toSet()
            }
        }
        viewModelScope.launch {
            mediaLoggerRepository.getMovies().collect { items ->
                val cutoff = System.currentTimeMillis() - oneYearMs
                _recentMovieTitles.value = items.filter { (it.dateCompleted ?: 0L) >= cutoff }.map { it.title }.toSet()
            }
        }
        viewModelScope.launch {
            mediaLoggerRepository.getGames().collect { items ->
                val cutoff = System.currentTimeMillis() - oneYearMs
                _recentGameTitles.value = items.filter { (it.dateCompleted ?: 0L) >= cutoff }.map { it.title }.toSet()
            }
        }
    }

    fun pickRandomBook() {
        val eligible = currentBooks.filter { it.title !in _recentBookTitles.value }
        val pool = eligible.ifEmpty { currentBooks }
        _state.update { it.copy(randomBook = pool.randomOrNull(), randomExcludedCount = currentBooks.size - pool.size) }
    }

    fun pickRandomMovie() {
        data class Candidate(val movie: PhysicalMovie, val itemTitle: String?)
        val candidates = currentMovies.flatMap { movie ->
            if (movie.isCollection && movie.collectionItems.isNotEmpty())
                movie.collectionItems.map { Candidate(movie, it.title) }
            else listOf(Candidate(movie, null))
        }
        val eligible = candidates.filter { (it.itemTitle ?: it.movie.title) !in _recentMovieTitles.value }
        val pool = eligible.ifEmpty { candidates }
        val pick = pool.randomOrNull()
        _state.update { it.copy(
            randomMovie = pick?.movie,
            randomMovieItemTitle = pick?.itemTitle,
            randomExcludedCount = candidates.size - pool.size
        ) }
    }

    fun pickRandomGame() {
        data class Candidate(val game: PhysicalGame, val itemTitle: String?)
        val candidates = currentGames.flatMap { game ->
            if (game.isCollection && game.collectionItems.isNotEmpty())
                game.collectionItems.map { Candidate(game, it.title) }
            else listOf(Candidate(game, null))
        }
        val eligible = candidates.filter { (it.itemTitle ?: it.game.title) !in _recentGameTitles.value }
        val pool = eligible.ifEmpty { candidates }
        val pick = pool.randomOrNull()
        _state.update { it.copy(
            randomGame = pick?.game,
            randomGameItemTitle = pick?.itemTitle,
            randomExcludedCount = candidates.size - pool.size
        ) }
    }

    fun discardPendingCover(localPath: String?) {
        if (localPath == null) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            imageCacheService.deleteCachedImage(localPath)
        }
    }

    fun selectTab(tab: PhysicalMediaTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun getBookById(id: Long): PhysicalBook? = currentBooks.find { it.id == id }
    fun getMovieById(id: Long): PhysicalMovie? = currentMovies.find { it.id == id }
    fun getGameById(id: Long): PhysicalGame? = currentGames.find { it.id == id }
    fun getTvSeriesById(id: Long): PhysicalTvSeries? = currentTvSeries.find { it.id == id }

    fun addBook(title: String, author: String, format: BookFormat, coverUrl: String?, coverLocalPath: String?, seriesName: String? = null, seriesNumber: Float? = null) {
        viewModelScope.launch {
            repository.insertBook(
                PhysicalBookEntity(
                    title = title,
                    author = author,
                    format = format,
                    coverUrl = coverUrl,
                    coverLocalPath = coverLocalPath,
                    seriesName = seriesName,
                    seriesNumber = seriesNumber
                )
            )
        }
    }

    fun updateBook(id: Long, title: String, author: String, format: BookFormat, coverUrl: String?, coverLocalPath: String?, seriesName: String? = null, seriesNumber: Float? = null) {
        viewModelScope.launch {
            val existing = repository.getBookById(id) ?: return@launch
            if (coverLocalPath != existing.coverLocalPath) {
                existing.coverLocalPath?.let { imageCacheService.deleteCachedImage(it) }
            }
            repository.updateBook(
                existing.copy(
                    title = title,
                    author = author,
                    format = format,
                    coverUrl = if (coverLocalPath != existing.coverLocalPath) coverUrl else existing.coverUrl,
                    coverLocalPath = coverLocalPath,
                    seriesName = seriesName,
                    seriesNumber = seriesNumber
                )
            )
        }
    }

    fun deleteBook(id: Long) {
        viewModelScope.launch {
            val existing = repository.getBookById(id)
            imageCacheService.deleteCachedImage(existing?.coverLocalPath)
            repository.deleteBook(id)
        }
    }

    fun addMovie(title: String, format: MovieFormat, limitedEdition: Boolean, steelbook: Boolean, slipcover: Boolean, boutiqueLabel: String?, catalogNumber: String?, coverUrl: String?, coverLocalPath: String?, isCollection: Boolean = false, collectionItems: List<String> = emptyList()) {
        viewModelScope.launch {
            val id = repository.insertMovie(
                PhysicalMovieEntity(
                    title = title,
                    format = format,
                    limitedEdition = limitedEdition,
                    steelbook = steelbook,
                    slipcover = slipcover,
                    boutiqueLabel = boutiqueLabel,
                    catalogNumber = catalogNumber,
                    coverUrl = coverUrl,
                    coverLocalPath = coverLocalPath,
                    isCollection = isCollection
                )
            )
            if (isCollection && collectionItems.isNotEmpty()) {
                repository.replaceMovieCollectionItems(id, collectionItems.filter { it.isNotBlank() })
            }
            preferences.setLastMovieFormat(format)
        }
    }

    fun updateMovie(id: Long, title: String, format: MovieFormat, limitedEdition: Boolean, steelbook: Boolean, slipcover: Boolean, boutiqueLabel: String?, catalogNumber: String?, coverUrl: String?, coverLocalPath: String?, isCollection: Boolean = false, collectionItems: List<String> = emptyList()) {
        viewModelScope.launch {
            val existing = repository.getMovieById(id) ?: return@launch
            if (coverLocalPath != existing.coverLocalPath) {
                existing.coverLocalPath?.let { imageCacheService.deleteCachedImage(it) }
            }
            repository.updateMovie(
                existing.copy(
                    title = title,
                    format = format,
                    limitedEdition = limitedEdition,
                    steelbook = steelbook,
                    slipcover = slipcover,
                    boutiqueLabel = boutiqueLabel,
                    catalogNumber = catalogNumber,
                    coverUrl = if (coverLocalPath != existing.coverLocalPath) coverUrl else existing.coverUrl,
                    coverLocalPath = coverLocalPath,
                    isCollection = isCollection
                )
            )
            repository.replaceMovieCollectionItems(id, if (isCollection) collectionItems.filter { it.isNotBlank() } else emptyList())
        }
    }

    fun deleteMovie(id: Long) {
        viewModelScope.launch {
            val existing = repository.getMovieById(id)
            imageCacheService.deleteCachedImage(existing?.coverLocalPath)
            repository.deleteMovie(id)
        }
    }

    fun addGame(title: String, system: GameSystem, coverUrl: String?, coverLocalPath: String?, isCollection: Boolean = false, collectionItems: List<String> = emptyList()) {
        viewModelScope.launch {
            val id = repository.insertGame(
                PhysicalGameEntity(
                    title = title,
                    system = system,
                    coverUrl = coverUrl,
                    coverLocalPath = coverLocalPath,
                    isCollection = isCollection
                )
            )
            if (isCollection && collectionItems.isNotEmpty()) {
                repository.replaceGameCollectionItems(id, collectionItems.filter { it.isNotBlank() })
            }
            preferences.setLastGameSystem(system)
        }
    }

    fun updateGame(id: Long, title: String, system: GameSystem, coverUrl: String?, coverLocalPath: String?, isCollection: Boolean = false, collectionItems: List<String> = emptyList()) {
        viewModelScope.launch {
            val existing = repository.getGameById(id) ?: return@launch
            if (coverLocalPath != existing.coverLocalPath) {
                existing.coverLocalPath?.let { imageCacheService.deleteCachedImage(it) }
            }
            repository.updateGame(
                existing.copy(
                    title = title,
                    system = system,
                    coverUrl = if (coverLocalPath != existing.coverLocalPath) coverUrl else existing.coverUrl,
                    coverLocalPath = coverLocalPath,
                    isCollection = isCollection
                )
            )
            repository.replaceGameCollectionItems(id, if (isCollection) collectionItems.filter { it.isNotBlank() } else emptyList())
        }
    }

    fun deleteGame(id: Long) {
        viewModelScope.launch {
            val existing = repository.getGameById(id)
            imageCacheService.deleteCachedImage(existing?.coverLocalPath)
            repository.deleteGame(id)
        }
    }

    fun addTvSeries(title: String, format: MovieFormat, completeSeries: Boolean, seriesName: String?, seriesNumber: Float?, coverUrl: String?, coverLocalPath: String?) {
        viewModelScope.launch {
            repository.insertTvSeries(
                PhysicalTvSeriesEntity(
                    title = title,
                    format = format,
                    completeSeries = completeSeries,
                    seriesName = seriesName,
                    seriesNumber = seriesNumber,
                    coverUrl = coverUrl,
                    coverLocalPath = coverLocalPath
                )
            )
        }
    }

    fun updateTvSeries(id: Long, title: String, format: MovieFormat, completeSeries: Boolean, seriesName: String?, seriesNumber: Float?, coverUrl: String?, coverLocalPath: String?) {
        viewModelScope.launch {
            val existing = repository.getTvSeriesById(id) ?: return@launch
            if (coverLocalPath != existing.coverLocalPath) {
                existing.coverLocalPath?.let { imageCacheService.deleteCachedImage(it) }
            }
            repository.updateTvSeries(
                existing.copy(
                    title = title,
                    format = format,
                    completeSeries = completeSeries,
                    seriesName = seriesName,
                    seriesNumber = seriesNumber,
                    coverUrl = if (coverLocalPath != existing.coverLocalPath) coverUrl else existing.coverUrl,
                    coverLocalPath = coverLocalPath
                )
            )
        }
    }

    fun deleteTvSeries(id: Long) {
        viewModelScope.launch {
            val existing = repository.getTvSeriesById(id)
            imageCacheService.deleteCachedImage(existing?.coverLocalPath)
            repository.deleteTvSeries(id)
        }
    }

    fun setDrillDown(filter: PhysicalMediaDrillDown?) {
        _state.update { it.copy(drillDown = filter) }
    }

    fun fetchGamePrices() {
        if (_state.value.isFetchingPrices) return
        viewModelScope.launch {
            val games = currentGames
            _state.update { it.copy(isFetchingPrices = true, gamePrices = emptyMap(), pricesFetchCount = 0) }
            val prices = mutableMapOf<Long, Double?>()
            games.forEachIndexed { index, game ->
                prices[game.id] = priceChartingService.fetchCibPrice(game.title, game.system)
                _state.update { it.copy(gamePrices = prices.toMap(), pricesFetchCount = index + 1) }
                if (index < games.size - 1) kotlinx.coroutines.delay(600)
            }
            _state.update { it.copy(isFetchingPrices = false) }
        }
    }

    private fun computeStats(): PhysicalMediaStats {
        val currentYear = java.time.LocalDate.now().year
        fun Long.isThisYear(): Boolean =
            Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).year == currentYear

        val addedThisYear = currentBooks.count { it.createdAt.isThisYear() } +
                currentMovies.count { it.createdAt.isThisYear() } +
                currentGames.count { it.createdAt.isThisYear() } +
                currentTvSeries.count { it.createdAt.isThisYear() }

        fun itemCount(isCollection: Boolean, itemCount: Int) =
            if (isCollection && itemCount > 0) itemCount else 1

        val gamesBySystem = currentGames
            .groupBy { it.system }
            .mapValues { (_, games) -> games.sumOf { itemCount(it.isCollection, it.collectionItems.size) } }
            .filter { it.value > 0 }

        val top3Systems = gamesBySystem.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key to it.value }

        return PhysicalMediaStats(
            totalBooks = currentBooks.size,
            totalMovies = currentMovies.sumOf { if (it.isCollection && it.collectionItems.isNotEmpty()) it.collectionItems.size else 1 },
            totalGames = currentGames.sumOf { if (it.isCollection && it.collectionItems.isNotEmpty()) it.collectionItems.size else 1 },
            totalTvSeries = currentTvSeries.size,
            booksByFormat = currentBooks.groupBy { it.format }.mapValues { it.value.size },
            moviesByFormat = currentMovies.groupBy { it.format }.mapValues { (_, movies) -> movies.sumOf { itemCount(it.isCollection, it.collectionItems.size) } },
            tvSeriesByFormat = currentTvSeries.groupBy { it.format }.mapValues { it.value.size },
            steelbookCount = currentMovies.count { it.steelbook },
            limitedEditionCount = currentMovies.count { it.limitedEdition },
            boutiqueLabelCount = currentMovies.count { it.boutiqueLabel != null },
            standardMovieCount = currentMovies.count { it.boutiqueLabel == null },
            moviesByBoutiqueLabel = currentMovies
                .filter { it.boutiqueLabel != null }
                .groupBy { it.boutiqueLabel!! }
                .mapValues { it.value.size }
                .entries.sortedByDescending { it.value }
                .associate { it.key to it.value },
            completeTvSeriesCount = currentTvSeries.count { it.completeSeries },
            gamesBySystem = gamesBySystem,
            top3Systems = top3Systems,
            addedThisYear = addedThisYear,
            totalItems = currentBooks.size +
                currentMovies.sumOf { if (it.isCollection && it.collectionItems.isNotEmpty()) it.collectionItems.size else 1 } +
                currentGames.sumOf { if (it.isCollection && it.collectionItems.isNotEmpty()) it.collectionItems.size else 1 } +
                currentTvSeries.size
        )
    }

    private fun PhysicalBookEntity.toDomain() = PhysicalBook(
        id = id, title = title, author = author, format = format,
        coverUrl = coverUrl, coverLocalPath = coverLocalPath,
        seriesName = seriesName, seriesNumber = seriesNumber, createdAt = createdAt
    )

    private fun PhysicalMovieEntity.toDomain(items: List<PhysicalMovieCollectionItemEntity> = emptyList()) = PhysicalMovie(
        id = id, title = title, format = format,
        limitedEdition = limitedEdition, steelbook = steelbook, slipcover = slipcover,
        boutiqueLabel = boutiqueLabel, catalogNumber = catalogNumber,
        coverUrl = coverUrl, coverLocalPath = coverLocalPath,
        isCollection = isCollection,
        collectionItems = items.map { PhysicalMovieCollectionItem(it.id, it.movieId, it.title) },
        createdAt = createdAt
    )

    private fun PhysicalGameEntity.toDomain(items: List<PhysicalGameCollectionItemEntity> = emptyList()) = PhysicalGame(
        id = id, title = title, system = system,
        coverUrl = coverUrl, coverLocalPath = coverLocalPath,
        isCollection = isCollection,
        collectionItems = items.map { PhysicalGameCollectionItem(it.id, it.gameId, it.title) },
        createdAt = createdAt
    )

    private fun PhysicalTvSeriesEntity.toDomain() = PhysicalTvSeries(
        id = id, title = title, format = format,
        completeSeries = completeSeries, seriesName = seriesName, seriesNumber = seriesNumber,
        coverUrl = coverUrl, coverLocalPath = coverLocalPath, createdAt = createdAt
    )
}
