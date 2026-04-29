package com.lifeos.modules.lifeos_physicalmedia.domain.model

import com.lifeos.modules.lifeos_physicalmedia.data.local.BookFormat
import com.lifeos.modules.lifeos_physicalmedia.data.local.GameSystem
import com.lifeos.modules.lifeos_physicalmedia.data.local.MovieFormat

data class PhysicalMediaStats(
    val totalBooks: Int = 0,
    val totalMovies: Int = 0,
    val totalGames: Int = 0,
    val booksByFormat: Map<BookFormat, Int> = emptyMap(),
    val moviesByFormat: Map<MovieFormat, Int> = emptyMap(),
    val steelbookCount: Int = 0,
    val limitedEditionCount: Int = 0,
    val gamesBySystem: Map<GameSystem, Int> = emptyMap(),
    val top3Systems: List<Pair<GameSystem, Int>> = emptyList(),
    val addedThisYear: Int = 0,
    val totalItems: Int = 0
)
