package com.lifeos.modules.lifeos_medialogger.data.repository

import com.lifeos.modules.lifeos_medialogger.data.local.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaLoggerRepository @Inject constructor(
    private val mediaItemDao: MediaItemDao,
    private val mangaSeriesDao: MangaSeriesDao,
    private val mangaVolumeDao: MangaVolumeDao
) {
    fun getBooks(): Flow<List<MediaItemEntity>> = mediaItemDao.getItemsByType(MediaType.BOOK)
    fun getMovies(): Flow<List<MediaItemEntity>> = mediaItemDao.getItemsByType(MediaType.MOVIE)
    fun getGames(): Flow<List<MediaItemEntity>> = mediaItemDao.getItemsByType(MediaType.GAME)

    fun getAllMangaSeries(): Flow<List<MangaSeriesEntity>> = mangaSeriesDao.getAllSeries()
    fun getAllVolumes(): Flow<List<MangaVolumeEntity>> = mangaVolumeDao.getAllVolumes()
    fun getVolumesBySeries(seriesId: Long): Flow<List<MangaVolumeEntity>> =
        mangaVolumeDao.getVolumesBySeries(seriesId)

    suspend fun getMediaItemById(id: Long): MediaItemEntity? = mediaItemDao.getItemById(id)
    suspend fun getMangaSeriesById(id: Long): MangaSeriesEntity? = mangaSeriesDao.getSeriesById(id)
    suspend fun getMangaVolumeById(id: Long): MangaVolumeEntity? = mangaVolumeDao.getVolumeById(id)

    suspend fun insertMediaItem(item: MediaItemEntity): Long = mediaItemDao.insert(item)
    suspend fun insertMangaSeries(series: MangaSeriesEntity): Long = mangaSeriesDao.insert(series)
    suspend fun insertMangaVolume(volume: MangaVolumeEntity): Long = mangaVolumeDao.insert(volume)

    suspend fun updateMediaItem(item: MediaItemEntity) = mediaItemDao.update(item)
    suspend fun updateMangaSeries(series: MangaSeriesEntity) = mangaSeriesDao.update(series)
    suspend fun updateMangaVolume(volume: MangaVolumeEntity) = mangaVolumeDao.update(volume)

    suspend fun deleteMediaItem(id: Long) = mediaItemDao.deleteById(id)
    suspend fun deleteMangaSeries(id: Long) = mangaSeriesDao.deleteById(id)
    suspend fun deleteMangaVolume(id: Long) = mangaVolumeDao.deleteById(id)
}