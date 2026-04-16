package com.lifeos.modules.lifeos_medialogger.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MediaType {
    BOOK,
    MOVIE,
    GAME
}

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val coverUrl: String? = null,
    val coverLocalPath: String? = null,
    val rating: Float? = null,
    val dateCompleted: Long? = null,
    val notes: String? = null,
    val type: MediaType,
    val platform: String? = null,
    val author: String? = null,
    val isRewatch: Boolean = false,
    val hasPlatinum: Boolean = false,
    val has100Percent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "manga_series")
data class MangaSeriesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val coverUrl: String? = null,
    val coverLocalPath: String? = null,
    val author: String? = null,
    val dateCompleted: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "manga_volumes",
    foreignKeys = [
        ForeignKey(
            entity = MangaSeriesEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("seriesId")]
)
data class MangaVolumeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val seriesId: Long,
    val volumeNumber: Int,
    val rating: Float? = null,
    val dateCompleted: Long? = null,
    val coverUrl: String? = null,
    val coverLocalPath: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
