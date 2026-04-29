package com.lifeos.modules.lifeos_physicalmedia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_physicalmedia.data.local.*
import com.lifeos.modules.lifeos_physicalmedia.data.repository.PhysicalMediaRepository
import com.lifeos.modules.lifeos_physicalmedia.domain.model.*
import com.lifeos.modules.lifeos_physicalmedia.service.ImageCacheService
import com.lifeos.modules.lifeos_physicalmedia.service.ImageSearchService
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
    val stats: PhysicalMediaStats = PhysicalMediaStats(),
    val isLoading: Boolean = true
) {
    val distinctBookAuthors: List<String>
        get() = books.map { it.author }.filter { it.isNotBlank() }.distinct().sorted()
    val distinctBookSeriesNames: List<String>
        get() = books.mapNotNull { it.seriesName }.filter { it.isNotBlank() }.distinct().sorted()
    val bookSeriesNumbers: Map<String, List<Float>>
        get() = books.filter { it.seriesName != null && it.seriesNumber != null }
            .groupBy { it.seriesName!! }
            .mapValues { (_, bs) -> bs.mapNotNull { it.seriesNumber }.sorted() }
}

@HiltViewModel
class PhysicalMediaViewModel @Inject constructor(
    private val repository: PhysicalMediaRepository,
    val imageCacheService: ImageCacheService,
    val imageSearchService: ImageSearchService
) : ViewModel() {

    private val _state = MutableStateFlow(PhysicalMediaState())
    val state: StateFlow<PhysicalMediaState> = _state.asStateFlow()

    private var currentBooks: List<PhysicalBook> = emptyList()
    private var currentMovies: List<PhysicalMovie> = emptyList()
    private var currentGames: List<PhysicalGame> = emptyList()

    init {
        viewModelScope.launch {
            repository.getBooks().collect { entities ->
                currentBooks = entities.map { it.toDomain() }
                _state.update { it.copy(books = currentBooks, stats = computeStats(), isLoading = false) }
            }
        }
        viewModelScope.launch {
            repository.getMovies().collect { entities ->
                currentMovies = entities.map { it.toDomain() }
                _state.update { it.copy(movies = currentMovies, stats = computeStats()) }
            }
        }
        viewModelScope.launch {
            repository.getGames().collect { entities ->
                currentGames = entities.map { it.toDomain() }
                _state.update { it.copy(games = currentGames, stats = computeStats()) }
            }
        }
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

    fun addMovie(title: String, format: MovieFormat, limitedEdition: Boolean, steelbook: Boolean, slipcover: Boolean, coverUrl: String?, coverLocalPath: String?) {
        viewModelScope.launch {
            repository.insertMovie(
                PhysicalMovieEntity(
                    title = title,
                    format = format,
                    limitedEdition = limitedEdition,
                    steelbook = steelbook,
                    slipcover = slipcover,
                    coverUrl = coverUrl,
                    coverLocalPath = coverLocalPath
                )
            )
        }
    }

    fun updateMovie(id: Long, title: String, format: MovieFormat, limitedEdition: Boolean, steelbook: Boolean, slipcover: Boolean, coverUrl: String?, coverLocalPath: String?) {
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
                    coverUrl = if (coverLocalPath != existing.coverLocalPath) coverUrl else existing.coverUrl,
                    coverLocalPath = coverLocalPath
                )
            )
        }
    }

    fun deleteMovie(id: Long) {
        viewModelScope.launch {
            val existing = repository.getMovieById(id)
            imageCacheService.deleteCachedImage(existing?.coverLocalPath)
            repository.deleteMovie(id)
        }
    }

    fun addGame(title: String, system: GameSystem, coverUrl: String?, coverLocalPath: String?) {
        viewModelScope.launch {
            repository.insertGame(
                PhysicalGameEntity(
                    title = title,
                    system = system,
                    coverUrl = coverUrl,
                    coverLocalPath = coverLocalPath
                )
            )
        }
    }

    fun updateGame(id: Long, title: String, system: GameSystem, coverUrl: String?, coverLocalPath: String?) {
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
                    coverLocalPath = coverLocalPath
                )
            )
        }
    }

    fun deleteGame(id: Long) {
        viewModelScope.launch {
            val existing = repository.getGameById(id)
            imageCacheService.deleteCachedImage(existing?.coverLocalPath)
            repository.deleteGame(id)
        }
    }

    private fun computeStats(): PhysicalMediaStats {
        val currentYear = java.time.LocalDate.now().year
        fun Long.isThisYear(): Boolean =
            Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).year == currentYear

        val addedThisYear = currentBooks.count { it.createdAt.isThisYear() } +
                currentMovies.count { it.createdAt.isThisYear() } +
                currentGames.count { it.createdAt.isThisYear() }

        val gamesBySystem = currentGames
            .groupBy { it.system }
            .mapValues { it.value.size }
            .filter { it.value > 0 }

        val top3Systems = gamesBySystem.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key to it.value }

        return PhysicalMediaStats(
            totalBooks = currentBooks.size,
            totalMovies = currentMovies.size,
            totalGames = currentGames.size,
            booksByFormat = currentBooks.groupBy { it.format }.mapValues { it.value.size },
            moviesByFormat = currentMovies.groupBy { it.format }.mapValues { it.value.size },
            steelbookCount = currentMovies.count { it.steelbook },
            limitedEditionCount = currentMovies.count { it.limitedEdition },
            gamesBySystem = gamesBySystem,
            top3Systems = top3Systems,
            addedThisYear = addedThisYear,
            totalItems = currentBooks.size + currentMovies.size + currentGames.size
        )
    }

    private fun PhysicalBookEntity.toDomain() = PhysicalBook(
        id = id, title = title, author = author, format = format,
        coverUrl = coverUrl, coverLocalPath = coverLocalPath,
        seriesName = seriesName, seriesNumber = seriesNumber, createdAt = createdAt
    )

    private fun PhysicalMovieEntity.toDomain() = PhysicalMovie(
        id = id, title = title, format = format,
        limitedEdition = limitedEdition, steelbook = steelbook, slipcover = slipcover,
        coverUrl = coverUrl, coverLocalPath = coverLocalPath, createdAt = createdAt
    )

    private fun PhysicalGameEntity.toDomain() = PhysicalGame(
        id = id, title = title, system = system,
        coverUrl = coverUrl, coverLocalPath = coverLocalPath, createdAt = createdAt
    )
}
