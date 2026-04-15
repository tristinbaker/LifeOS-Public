package com.lifeos.modules.lifeos_medialogger.domain.model

import com.lifeos.modules.lifeos_medialogger.data.local.MediaType

data class MediaItem(
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
    val createdAt: Long = System.currentTimeMillis()
)

data class MangaSeries(
    val id: Long = 0,
    val title: String,
    val coverUrl: String? = null,
    val coverLocalPath: String? = null,
    val author: String? = null,
    val dateCompleted: Long? = null,
    val volumes: List<MangaVolume> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class MangaVolume(
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

enum class MediaTab {
    BOOKS,
    MOVIES,
    GAMES,
    STATS
}
