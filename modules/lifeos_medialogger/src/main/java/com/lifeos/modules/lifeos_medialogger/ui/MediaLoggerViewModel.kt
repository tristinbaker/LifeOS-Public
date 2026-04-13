package com.lifeos.modules.lifeos_medialogger.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_medialogger.data.local.*
import com.lifeos.modules.lifeos_medialogger.data.repository.MediaLoggerRepository
import com.lifeos.modules.lifeos_medialogger.domain.model.*
import com.lifeos.modules.lifeos_medialogger.service.ImageCacheService
import com.lifeos.modules.lifeos_medialogger.service.ImageSearchService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MediaLoggerState(
    val selectedTab: MediaTab = MediaTab.BOOKS,
    val books: List<MediaItem> = emptyList(),
    val movies: List<MediaItem> = emptyList(),
    val games: List<MediaItem> = emptyList(),
    val mangaSeries: List<MangaSeries> = emptyList(),
    val expandedSeriesIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MediaLoggerViewModel @Inject constructor(
    private val repository: MediaLoggerRepository,
    private val imageCacheService: ImageCacheService,
    val imageSearchService: ImageSearchService
) : ViewModel() {

    private val _state = MutableStateFlow(MediaLoggerState())
    val state: StateFlow<MediaLoggerState> = _state.asStateFlow()

    fun getBookById(id: Long): MediaItem? = _state.value.books.find { it.id == id }
    fun getMovieById(id: Long): MediaItem? = _state.value.movies.find { it.id == id }
    fun getGameById(id: Long): MediaItem? = _state.value.games.find { it.id == id }
    fun getSeriesById(id: Long): MangaSeries? = _state.value.mangaSeries.find { it.id == id }
    fun getVolumeById(id: Long): MangaVolume? =
        _state.value.mangaSeries.flatMap { it.volumes }.find { it.id == id }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            launch {
                repository.getBooks().collect { items ->
                    _state.update { it.copy(books = items.map { it.toMediaItem() }) }
                }
            }
            launch {
                repository.getMovies().collect { items ->
                    _state.update { it.copy(movies = items.map { it.toMediaItem() }) }
                }
            }
            launch {
                repository.getGames().collect { items ->
                    _state.update { it.copy(games = items.map { it.toMediaItem() }) }
                }
            }
            launch {
                combine(
                    repository.getAllMangaSeries(),
                    repository.getAllVolumes()
                ) { seriesList, allVolumes ->
                    seriesList.map { series ->
                        MangaSeries(
                            id = series.id,
                            title = series.title,
                            coverUrl = series.coverUrl,
                            coverLocalPath = series.coverLocalPath,
                            author = series.author,
                            dateCompleted = series.dateCompleted,
                            volumes = allVolumes
                                .filter { it.seriesId == series.id }
                                .map { it.toMangaVolume() },
                            createdAt = series.createdAt
                        )
                    }
                }.collect { seriesWithVolumes ->
                    _state.update { it.copy(mangaSeries = seriesWithVolumes, isLoading = false) }
                }
            }
        }
    }

    fun selectTab(tab: MediaTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun toggleSeriesExpanded(seriesId: Long) {
        _state.update { state ->
            val newExpanded = if (seriesId in state.expandedSeriesIds) {
                state.expandedSeriesIds - seriesId
            } else {
                state.expandedSeriesIds + seriesId
            }
            state.copy(expandedSeriesIds = newExpanded)
        }
    }

    fun addMediaItem(
        title: String,
        coverUrl: String?,
        rating: Float?,
        dateCompleted: Long?,
        notes: String?,
        type: MediaType,
        platform: String? = null,
        author: String? = null
    ) {
        viewModelScope.launch {
            val localPath = coverUrl?.let { imageCacheService.downloadAndCacheImage(it) }
            val item = MediaItemEntity(
                title = title,
                coverUrl = coverUrl,
                coverLocalPath = localPath,
                rating = rating,
                dateCompleted = dateCompleted,
                notes = notes,
                type = type,
                platform = platform,
                author = author
            )
            repository.insertMediaItem(item)
        }
    }

    fun updateMediaItem(
        id: Long,
        title: String,
        coverUrl: String?,
        rating: Float?,
        dateCompleted: Long?,
        notes: String?,
        platform: String? = null,
        author: String? = null
    ) {
        viewModelScope.launch {
            val existing = repository.getMediaItemById(id) ?: return@launch
            var localPath = existing.coverLocalPath

            if (coverUrl != existing.coverUrl) {
                existing.coverLocalPath?.let { imageCacheService.deleteCachedImage(it) }
                localPath = coverUrl?.let { imageCacheService.downloadAndCacheImage(it) }
            }

            repository.updateMediaItem(existing.copy(
                title = title,
                coverUrl = coverUrl,
                coverLocalPath = localPath,
                rating = rating,
                dateCompleted = dateCompleted,
                notes = notes,
                platform = platform,
                author = author
            ))
        }
    }

    fun deleteMediaItem(id: Long) {
        viewModelScope.launch {
            val item = repository.getMediaItemById(id)
            item?.coverLocalPath?.let { imageCacheService.deleteCachedImage(it) }
            repository.deleteMediaItem(id)
        }
    }

    fun addMangaSeries(title: String, coverUrl: String?, author: String?, dateCompleted: Long?) {
        viewModelScope.launch {
            val localPath = coverUrl?.let { imageCacheService.downloadAndCacheImage(it) }
            repository.insertMangaSeries(MangaSeriesEntity(
                title = title,
                coverUrl = coverUrl,
                coverLocalPath = localPath,
                author = author,
                dateCompleted = dateCompleted
            ))
        }
    }

    fun updateMangaSeries(id: Long, title: String, coverUrl: String?, author: String?, dateCompleted: Long?) {
        viewModelScope.launch {
            val existing = repository.getMangaSeriesById(id) ?: return@launch
            var localPath = existing.coverLocalPath

            if (coverUrl != existing.coverUrl) {
                existing.coverLocalPath?.let { imageCacheService.deleteCachedImage(it) }
                localPath = coverUrl?.let { imageCacheService.downloadAndCacheImage(it) }
            }

            repository.updateMangaSeries(existing.copy(
                title = title,
                coverUrl = coverUrl,
                coverLocalPath = localPath,
                author = author,
                dateCompleted = dateCompleted
            ))
        }
    }

    fun addMangaVolume(
        seriesId: Long,
        volumeNumber: Int,
        rating: Float?,
        dateCompleted: Long?,
        coverUrl: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val localPath = coverUrl?.let { imageCacheService.downloadAndCacheImage(it) }
            repository.insertMangaVolume(MangaVolumeEntity(
                seriesId = seriesId,
                volumeNumber = volumeNumber,
                rating = rating,
                dateCompleted = dateCompleted,
                coverUrl = coverUrl,
                coverLocalPath = localPath,
                notes = notes
            ))
        }
    }

    fun updateMangaVolume(
        id: Long,
        volumeNumber: Int,
        rating: Float?,
        dateCompleted: Long?,
        coverUrl: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val existing = repository.getMangaVolumeById(id) ?: return@launch
            var localPath = existing.coverLocalPath

            if (coverUrl != existing.coverUrl) {
                existing.coverLocalPath?.let { imageCacheService.deleteCachedImage(it) }
                localPath = coverUrl?.let { imageCacheService.downloadAndCacheImage(it) }
            }

            repository.updateMangaVolume(existing.copy(
                volumeNumber = volumeNumber,
                rating = rating,
                dateCompleted = dateCompleted,
                coverUrl = coverUrl,
                coverLocalPath = localPath,
                notes = notes
            ))
        }
    }

    fun deleteMangaSeries(id: Long) {
        viewModelScope.launch {
            val series = repository.getMangaSeriesById(id)
            series?.coverLocalPath?.let { imageCacheService.deleteCachedImage(it) }
            repository.deleteMangaSeries(id)
        }
    }

    fun deleteMangaVolume(id: Long) {
        viewModelScope.launch {
            val volume = repository.getMangaVolumeById(id)
            volume?.coverLocalPath?.let { imageCacheService.deleteCachedImage(it) }
            repository.deleteMangaVolume(id)
        }
    }
}

private fun MediaItemEntity.toMediaItem() = MediaItem(
    id = id,
    title = title,
    coverUrl = coverUrl,
    coverLocalPath = coverLocalPath,
    rating = rating,
    dateCompleted = dateCompleted,
    notes = notes,
    type = type,
    platform = platform,
    author = author,
    createdAt = createdAt
)

private fun MangaVolumeEntity.toMangaVolume() = MangaVolume(
    id = id,
    seriesId = seriesId,
    volumeNumber = volumeNumber,
    rating = rating,
    dateCompleted = dateCompleted,
    coverUrl = coverUrl,
    coverLocalPath = coverLocalPath,
    notes = notes,
    createdAt = createdAt
)
