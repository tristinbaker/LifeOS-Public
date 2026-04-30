package com.lifeos.modules.lifeos_physicalmedia.domain.model

import com.lifeos.modules.lifeos_physicalmedia.data.local.BookFormat
import com.lifeos.modules.lifeos_physicalmedia.data.local.GameSystem
import com.lifeos.modules.lifeos_physicalmedia.data.local.MovieFormat

data class PhysicalBook(
    val id: Long = 0,
    val title: String,
    val author: String,
    val format: BookFormat,
    val coverUrl: String? = null,
    val coverLocalPath: String? = null,
    val seriesName: String? = null,
    val seriesNumber: Float? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class PhysicalMovie(
    val id: Long = 0,
    val title: String,
    val format: MovieFormat,
    val limitedEdition: Boolean = false,
    val steelbook: Boolean = false,
    val slipcover: Boolean = false,
    val boutiqueLabel: String? = null,
    val catalogNumber: String? = null,
    val coverUrl: String? = null,
    val coverLocalPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class PhysicalGame(
    val id: Long = 0,
    val title: String,
    val system: GameSystem,
    val coverUrl: String? = null,
    val coverLocalPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class PhysicalTvSeries(
    val id: Long = 0,
    val title: String,
    val format: MovieFormat,
    val completeSeries: Boolean = false,
    val seriesName: String? = null,
    val seriesNumber: Float? = null,
    val coverUrl: String? = null,
    val coverLocalPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class PhysicalMediaTab { BOOKS, MOVIES, GAMES, TV_SERIES, STATS }
