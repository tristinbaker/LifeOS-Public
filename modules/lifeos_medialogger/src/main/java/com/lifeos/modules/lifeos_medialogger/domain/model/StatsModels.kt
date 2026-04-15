package com.lifeos.modules.lifeos_medialogger.domain.model

data class MediaStats(
    val thisYearCounts: YearCounts = YearCounts(),
    val allTimeCounts: YearCounts = YearCounts(),
    val thisYearAvgPerMonth: AvgPerMonth = AvgPerMonth(),
    val allTimeAvgPerMonth: AvgPerMonth = AvgPerMonth(),
    val lastYearCounts: YearCounts = YearCounts(),
    val ratingDistributions: RatingDistributions = RatingDistributions(),
    val topRated: TopRated = TopRated(),
    val mostConsumedMonth: MonthCount = MonthCount(),
    val rewatchRegameCounts: RewatchRegameCounts = RewatchRegameCounts()
)

data class YearCounts(
    val books: Int = 0,
    val movies: Int = 0,
    val games: Int = 0
)

data class AvgPerMonth(
    val books: Float = 0f,
    val movies: Float = 0f,
    val games: Float = 0f
)

data class RatingDistributions(
    val books: Map<Int, Int> = emptyMap(),
    val movies: Map<Int, Int> = emptyMap(),
    val games: Map<Int, Int> = emptyMap()
)

data class TopRated(
    val book: TopRatedItem? = null,
    val movie: TopRatedItem? = null,
    val game: TopRatedItem? = null
)

data class TopRatedItem(
    val title: String,
    val rating: Float,
    val dateCompleted: Long?
)

data class MonthCount(
    val month: String = "",
    val year: Int = 0,
    val count: Int = 0
)

data class RewatchRegameCounts(
    val movies: Int = 0,
    val games: Int = 0
)
