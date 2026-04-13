package com.lifeos.modules.lifeos_medialogger.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaItemDao {
    @Query("SELECT * FROM media_items WHERE type = :type ORDER BY dateCompleted DESC")
    fun getItemsByType(type: MediaType): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getItemById(id: Long): MediaItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MediaItemEntity): Long

    @Update
    suspend fun update(item: MediaItemEntity)

    @Delete
    suspend fun delete(item: MediaItemEntity)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface MangaSeriesDao {
    @Query("SELECT * FROM manga_series ORDER BY createdAt DESC")
    fun getAllSeries(): Flow<List<MangaSeriesEntity>>

    @Query("SELECT * FROM manga_series WHERE id = :id")
    suspend fun getSeriesById(id: Long): MangaSeriesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(series: MangaSeriesEntity): Long

    @Update
    suspend fun update(series: MangaSeriesEntity)

    @Delete
    suspend fun delete(series: MangaSeriesEntity)

    @Query("DELETE FROM manga_series WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface MangaVolumeDao {
    @Query("SELECT * FROM manga_volumes ORDER BY seriesId ASC, volumeNumber ASC")
    fun getAllVolumes(): Flow<List<MangaVolumeEntity>>

    @Query("SELECT * FROM manga_volumes WHERE seriesId = :seriesId ORDER BY volumeNumber ASC")
    fun getVolumesBySeries(seriesId: Long): Flow<List<MangaVolumeEntity>>

    @Query("SELECT * FROM manga_volumes WHERE id = :id")
    suspend fun getVolumeById(id: Long): MangaVolumeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(volume: MangaVolumeEntity): Long

    @Update
    suspend fun update(volume: MangaVolumeEntity)

    @Delete
    suspend fun delete(volume: MangaVolumeEntity)

    @Query("DELETE FROM manga_volumes WHERE id = :id")
    suspend fun deleteById(id: Long)
}