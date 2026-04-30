package com.lifeos.modules.lifeos_physicalmedia.domain.model

import com.lifeos.modules.lifeos_physicalmedia.data.local.BookFormat
import com.lifeos.modules.lifeos_physicalmedia.data.local.GameSystem
import com.lifeos.modules.lifeos_physicalmedia.data.local.MovieFormat

sealed class PhysicalMediaDrillDown {
    data class BooksByFormat(val format: BookFormat) : PhysicalMediaDrillDown()
    data class MoviesByFormat(val format: MovieFormat) : PhysicalMediaDrillDown()
    data object MoviesSteelbooks : PhysicalMediaDrillDown()
    data object MoviesLimitedEditions : PhysicalMediaDrillDown()
    data object MoviesBoutique : PhysicalMediaDrillDown()
    data object MoviesStandard : PhysicalMediaDrillDown()
    data class MoviesByBoutiqueLabel(val label: String) : PhysicalMediaDrillDown()
    data class TvByFormat(val format: MovieFormat) : PhysicalMediaDrillDown()
    data object TvCompleteSeries : PhysicalMediaDrillDown()
    data class GamesBySystem(val system: GameSystem) : PhysicalMediaDrillDown()
}

fun PhysicalMediaDrillDown.title(): String = when (this) {
    is PhysicalMediaDrillDown.BooksByFormat -> "${format.displayName()} Books"
    is PhysicalMediaDrillDown.MoviesByFormat -> "${format.displayName()} Movies"
    is PhysicalMediaDrillDown.MoviesSteelbooks -> "Steelbooks"
    is PhysicalMediaDrillDown.MoviesLimitedEditions -> "Limited Editions"
    is PhysicalMediaDrillDown.MoviesBoutique -> "Boutique Movies"
    is PhysicalMediaDrillDown.MoviesStandard -> "Standard Movies"
    is PhysicalMediaDrillDown.MoviesByBoutiqueLabel -> label
    is PhysicalMediaDrillDown.TvByFormat -> "${format.displayName()} TV"
    is PhysicalMediaDrillDown.TvCompleteSeries -> "Complete Series"
    is PhysicalMediaDrillDown.GamesBySystem -> "${system.displayName()} Games"
}

data class PhysicalMediaStats(
    val totalBooks: Int = 0,
    val totalMovies: Int = 0,
    val totalGames: Int = 0,
    val totalTvSeries: Int = 0,
    val booksByFormat: Map<BookFormat, Int> = emptyMap(),
    val moviesByFormat: Map<MovieFormat, Int> = emptyMap(),
    val tvSeriesByFormat: Map<MovieFormat, Int> = emptyMap(),
    val steelbookCount: Int = 0,
    val limitedEditionCount: Int = 0,
    val boutiqueLabelCount: Int = 0,
    val standardMovieCount: Int = 0,
    val moviesByBoutiqueLabel: Map<String, Int> = emptyMap(),
    val completeTvSeriesCount: Int = 0,
    val gamesBySystem: Map<GameSystem, Int> = emptyMap(),
    val top3Systems: List<Pair<GameSystem, Int>> = emptyList(),
    val addedThisYear: Int = 0,
    val totalItems: Int = 0
)
