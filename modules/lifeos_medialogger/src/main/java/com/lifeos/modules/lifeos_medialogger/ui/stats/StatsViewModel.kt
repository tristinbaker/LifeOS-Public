package com.lifeos.modules.lifeos_medialogger.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_medialogger.data.repository.MediaLoggerRepository
import com.lifeos.modules.lifeos_medialogger.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

data class StatsUiState(
    val stats: MediaStats = MediaStats(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: MediaLoggerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            combine(
                repository.getBooks(),
                repository.getMovies(),
                repository.getGames(),
                repository.getAllMangaSeries()
            ) { books, movies, games, mangaSeries ->
                val booksList = books.map { it.toMediaItem() }
                val moviesList = movies.map { it.toMediaItem() }
                val gamesList = games.map { it.toMediaItem() }
                val mangaList = mangaSeries.map { series ->
                    MangaSeries(
                        id = series.id,
                        title = series.title,
                        coverUrl = series.coverUrl,
                        coverLocalPath = series.coverLocalPath,
                        author = series.author,
                        dateCompleted = series.dateCompleted,
                        volumes = emptyList(),
                        createdAt = series.createdAt
                    )
                }

                calculateStats(booksList, moviesList, gamesList, mangaList)
            }.collect { stats ->
                _state.value = StatsUiState(stats = stats, isLoading = false)
            }
        }
    }

    private fun calculateStats(
        books: List<MediaItem>,
        movies: List<MediaItem>,
        games: List<MediaItem>,
        mangaSeries: List<MangaSeries>
    ): MediaStats {
        val currentYear = LocalDate.now().year
        val lastYear = currentYear - 1

        val booksAndMangaThisYear = countItemsInYear(books, currentYear) + countSeriesInYear(mangaSeries, currentYear)
        val booksAndMangaLastYear = countItemsInYear(books, lastYear) + countSeriesInYear(mangaSeries, lastYear)
        val booksAndMangaAllTime = books.size + mangaSeries.size

        val thisYearCounts = YearCounts(
            books = booksAndMangaThisYear,
            movies = countItemsInYear(movies, currentYear),
            games = countItemsInYear(games, currentYear)
        )

        val lastYearCounts = YearCounts(
            books = booksAndMangaLastYear,
            movies = countItemsInYear(movies, lastYear),
            games = countItemsInYear(games, lastYear)
        )

        val allTimeCounts = YearCounts(
            books = booksAndMangaAllTime,
            movies = movies.size,
            games = games.size
        )

        val monthsThisYear = (LocalDate.now().monthValue).coerceAtLeast(1)
        val thisYearAvgPerMonth = AvgPerMonth(
            books = if (monthsThisYear > 0) thisYearCounts.books.toFloat() / monthsThisYear else 0f,
            movies = if (monthsThisYear > 0) thisYearCounts.movies.toFloat() / monthsThisYear else 0f,
            games = if (monthsThisYear > 0) thisYearCounts.games.toFloat() / monthsThisYear else 0f
        )

        val allYearsMonths = calculateTotalMonths(books, movies, games, mangaSeries)
        val allTimeAvgPerMonth = AvgPerMonth(
            books = if (allYearsMonths > 0) allTimeCounts.books.toFloat() / allYearsMonths else 0f,
            movies = if (allYearsMonths > 0) allTimeCounts.movies.toFloat() / allYearsMonths else 0f,
            games = if (allYearsMonths > 0) allTimeCounts.games.toFloat() / allYearsMonths else 0f
        )

        val ratingDistributions = RatingDistributions(
            books = calculateRatingDistribution(books),
            movies = calculateRatingDistribution(movies),
            games = calculateRatingDistribution(games)
        )

        val allBooks = books + mangaSeries.map { series ->
            MediaItem(
                id = series.id,
                title = series.title,
                type = com.lifeos.modules.lifeos_medialogger.data.local.MediaType.BOOK,
                dateCompleted = series.dateCompleted,
                rating = null,
                createdAt = series.createdAt
            )
        }
        val topRated = TopRated(
            book = findTopRated(allBooks),
            movie = findTopRated(movies),
            game = findTopRated(games)
        )

        val mostConsumedMonth = findMostConsumedMonth(books, movies, games, mangaSeries, currentYear)

        val rewatchRegameCounts = RewatchRegameCounts(
            movies = movies.count { it.isRewatch },
            games = games.count { it.isRewatch }
        )

        return MediaStats(
            thisYearCounts = thisYearCounts,
            allTimeCounts = allTimeCounts,
            thisYearAvgPerMonth = thisYearAvgPerMonth,
            allTimeAvgPerMonth = allTimeAvgPerMonth,
            lastYearCounts = lastYearCounts,
            ratingDistributions = ratingDistributions,
            topRated = topRated,
            mostConsumedMonth = mostConsumedMonth,
            rewatchRegameCounts = rewatchRegameCounts
        )
    }

    private fun countItemsInYear(items: List<MediaItem>, year: Int): Int {
        return items.count { item ->
            item.dateCompleted?.let { timestamp ->
                val itemYear = LocalDate.ofInstant(
                    java.time.Instant.ofEpochMilli(timestamp),
                    ZoneId.systemDefault()
                ).year
                itemYear == year
            } ?: false
        }
    }

    private fun countSeriesInYear(series: List<MangaSeries>, year: Int): Int {
        return series.count { item ->
            item.dateCompleted?.let { timestamp ->
                val itemYear = LocalDate.ofInstant(
                    java.time.Instant.ofEpochMilli(timestamp),
                    ZoneId.systemDefault()
                ).year
                itemYear == year
            } ?: false
        }
    }

    private fun calculateTotalMonths(
        books: List<MediaItem>,
        movies: List<MediaItem>,
        games: List<MediaItem>,
        mangaSeries: List<MangaSeries>
    ): Int {
        val allItems = (books + movies + games).filter { it.dateCompleted != null }
        val seriesWithDates = mangaSeries.filter { it.dateCompleted != null }

        if (allItems.isEmpty() && seriesWithDates.isEmpty()) return 0

        val dates = allItems.mapNotNull { it.dateCompleted }.plus(seriesWithDates.mapNotNull { it.dateCompleted })
        if (dates.isEmpty()) return 0

        val earliestDate = dates.minOrNull() ?: return 0
        val earliest = LocalDate.ofInstant(
            java.time.Instant.ofEpochMilli(earliestDate),
            ZoneId.systemDefault()
        )

        val current = LocalDate.now()
        val months = (current.year - earliest.year) * 12 + (current.monthValue - earliest.monthValue) + 1
        return months.coerceAtLeast(1)
    }

    private fun calculateRatingDistribution(items: List<MediaItem>): Map<Int, Int> {
        val distribution = mutableMapOf<Int, Int>()
        for (i in 1..5) {
            distribution[i] = items.count { it.rating?.toInt() == i }
        }
        return distribution
    }

    private fun findTopRated(items: List<MediaItem>): TopRatedItem? {
        val withRating = items.filter { it.rating != null && it.rating > 0 }
        if (withRating.isEmpty()) return null

        val maxRating = withRating.maxOf { it.rating!! }
        val topRated = withRating.filter { it.rating == maxRating }
        val mostRecent = topRated.maxByOrNull { it.dateCompleted ?: 0L }

        return mostRecent?.let {
            TopRatedItem(
                title = it.title,
                rating = it.rating!!,
                dateCompleted = it.dateCompleted
            )
        }
    }

    private fun findTopRatedSeries(series: List<MangaSeries>): TopRatedItem? {
        val withRating = series.filter { it.dateCompleted != null }
        if (withRating.isEmpty()) return null

        val mostRecent = withRating.maxByOrNull { it.dateCompleted ?: 0L }

        return mostRecent?.let {
            TopRatedItem(
                title = it.title,
                rating = 5f,
                dateCompleted = it.dateCompleted
            )
        }
    }

    private fun findMostConsumedMonth(
        books: List<MediaItem>,
        movies: List<MediaItem>,
        games: List<MediaItem>,
        mangaSeries: List<MangaSeries>,
        year: Int
    ): MonthCount {
        val monthCounts = mutableMapOf<Int, Int>()

        for (month in 1..12) {
            val bookCount = books.count { item ->
                item.dateCompleted?.let { timestamp ->
                    val date = LocalDate.ofInstant(
                        java.time.Instant.ofEpochMilli(timestamp),
                        ZoneId.systemDefault()
                    )
                    date.year == year && date.monthValue == month
                } ?: false
            }
            val movieCount = movies.count { item ->
                item.dateCompleted?.let { timestamp ->
                    val date = LocalDate.ofInstant(
                        java.time.Instant.ofEpochMilli(timestamp),
                        ZoneId.systemDefault()
                    )
                    date.year == year && date.monthValue == month
                } ?: false
            }
            val gameCount = games.count { item ->
                item.dateCompleted?.let { timestamp ->
                    val date = LocalDate.ofInstant(
                        java.time.Instant.ofEpochMilli(timestamp),
                        ZoneId.systemDefault()
                    )
                    date.year == year && date.monthValue == month
                } ?: false
            }
            val mangaCount = mangaSeries.count { item ->
                item.dateCompleted?.let { timestamp ->
                    val date = LocalDate.ofInstant(
                        java.time.Instant.ofEpochMilli(timestamp),
                        ZoneId.systemDefault()
                    )
                    date.year == year && date.monthValue == month
                } ?: false
            }

            monthCounts[month] = bookCount + movieCount + gameCount + mangaCount
        }

        val maxMonth = monthCounts.maxByOrNull { it.value }?.key ?: return MonthCount()
        val maxCount = monthCounts[maxMonth] ?: 0

        if (maxCount == 0) return MonthCount()

        val monthName = LocalDate.of(year, maxMonth, 1).format(DateTimeFormatter.ofPattern("MMMM"))

        return MonthCount(
            month = monthName,
            year = year,
            count = maxCount
        )
    }

    private fun com.lifeos.modules.lifeos_medialogger.data.local.MediaItemEntity.toMediaItem() = MediaItem(
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
        isRewatch = isRewatch,
        createdAt = createdAt
    )
}
