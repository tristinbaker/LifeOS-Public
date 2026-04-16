package com.lifeos.modules.lifeos_medialogger.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_medialogger.data.local.MediaType
import com.lifeos.modules.lifeos_medialogger.data.repository.MediaLoggerRepository
import com.lifeos.modules.lifeos_medialogger.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class StatsUiState(
    val stats: MediaStats = MediaStats(),
    val isLoading: Boolean = true,
    val detailStats: DetailStats? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: MediaLoggerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    private val _detailType = MutableStateFlow<MediaType?>(null)
    private val _detailYear = MutableStateFlow<Int?>(null)

    init {
        loadStats()
    }

    fun setDetailType(type: MediaType) {
        _detailType.value = type
        _detailYear.value = null
    }

    fun setDetailYear(year: Int?) {
        _detailYear.value = year
    }

    private fun loadStats() {
        viewModelScope.launch {
            combine(
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
                    RawData(booksList, moviesList, gamesList, mangaList)
                },
                _detailType,
                _detailYear
            ) { raw, type, year ->
                val stats = calculateStats(raw.books, raw.movies, raw.games, raw.manga)
                val detail = type?.let {
                    val items = when (it) {
                        MediaType.BOOK  -> raw.books
                        MediaType.MOVIE -> raw.movies
                        MediaType.GAME  -> raw.games
                    }
                    calculateDetailStats(items, it, year)
                }
                Pair(stats, detail)
            }.collect { (stats, detail) ->
                _state.value = StatsUiState(stats = stats, isLoading = false, detailStats = detail)
            }
        }
    }

    private data class RawData(
        val books: List<MediaItem>,
        val movies: List<MediaItem>,
        val games: List<MediaItem>,
        val manga: List<MangaSeries>
    )

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
                type = MediaType.BOOK,
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

    private fun calculateDetailStats(
        items: List<MediaItem>,
        type: MediaType,
        year: Int?
    ): DetailStats {
        val availableYears = items
            .mapNotNull { it.dateCompleted }
            .map { LocalDate.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()).year }
            .distinct()
            .sortedDescending()

        val filtered = if (year == null) items else items.filter { item ->
            item.dateCompleted?.let {
                LocalDate.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()).year == year
            } ?: false
        }

        val totalCount = filtered.size
        val rated = filtered.filter { it.rating != null && it.rating > 0f }
        val avgRating = if (rated.isEmpty()) null else rated.map { it.rating!! }.average().toFloat()

        val ratingDistribution = listOf(5f, 4.5f, 4f, 3.5f, 3f, 2.5f, 2f, 1.5f, 1f, 0.5f)
            .associateWith { r -> filtered.count { it.rating == r } }
            .filter { it.value > 0 }

        val topRated = findTopRated(filtered)
        val mostActiveMonth = findMostActiveMonthForItems(filtered)

        val rewatchCount = if (type == MediaType.BOOK) 0 else filtered.count { it.isRewatch }
        val rewatchPercent = if (totalCount > 0 && type != MediaType.BOOK)
            rewatchCount.toFloat() / totalCount * 100f else 0f

        val platformBreakdown = if (type == MediaType.GAME)
            filtered.mapNotNull { it.platform }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .map { it.key to it.value }
        else emptyList()

        val platinumCount = if (type == MediaType.GAME) filtered.count { it.hasPlatinum } else 0
        val hundredPercentCount = if (type == MediaType.GAME) filtered.count { it.has100Percent } else 0

        val topAuthors = if (type == MediaType.BOOK)
            filtered.mapNotNull { it.author }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(10)
                .map { it.key to it.value }
        else emptyList()

        val sortedItems = filtered.sortedWith(
            compareByDescending<MediaItem> { it.dateCompleted ?: 0L }.thenByDescending { it.createdAt }
        )

        return DetailStats(
            type = type,
            year = year,
            totalCount = totalCount,
            avgRating = avgRating,
            ratingDistribution = ratingDistribution,
            topRated = topRated,
            mostActiveMonth = mostActiveMonth,
            rewatchCount = rewatchCount,
            rewatchPercent = rewatchPercent,
            platformBreakdown = platformBreakdown,
            platinumCount = platinumCount,
            hundredPercentCount = hundredPercentCount,
            topAuthors = topAuthors,
            items = sortedItems,
            availableYears = availableYears
        )
    }

    private fun findMostActiveMonthForItems(items: List<MediaItem>): MonthCount {
        val monthCounts = items.mapNotNull { it.dateCompleted }
            .map { LocalDate.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) }
            .groupingBy { it.year to it.monthValue }
            .eachCount()
        if (monthCounts.isEmpty()) return MonthCount()
        val (ym, count) = monthCounts.maxByOrNull { it.value } ?: return MonthCount()
        val name = LocalDate.of(ym.first, ym.second, 1)
            .format(DateTimeFormatter.ofPattern("MMMM"))
        return MonthCount(month = name, year = ym.first, count = count)
    }

    private fun countItemsInYear(items: List<MediaItem>, year: Int): Int {
        return items.count { item ->
            item.dateCompleted?.let { timestamp ->
                LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).year == year
            } ?: false
        }
    }

    private fun countSeriesInYear(series: List<MangaSeries>, year: Int): Int {
        return series.count { item ->
            item.dateCompleted?.let { timestamp ->
                LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).year == year
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

        val dates = allItems.mapNotNull { it.dateCompleted }
            .plus(seriesWithDates.mapNotNull { it.dateCompleted })
        if (dates.isEmpty()) return 0

        val earliestDate = dates.minOrNull() ?: return 0
        val earliest = LocalDate.ofInstant(Instant.ofEpochMilli(earliestDate), ZoneId.systemDefault())
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
                    val date = LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                    date.year == year && date.monthValue == month
                } ?: false
            }
            val movieCount = movies.count { item ->
                item.dateCompleted?.let { timestamp ->
                    val date = LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                    date.year == year && date.monthValue == month
                } ?: false
            }
            val gameCount = games.count { item ->
                item.dateCompleted?.let { timestamp ->
                    val date = LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                    date.year == year && date.monthValue == month
                } ?: false
            }
            val mangaCount = mangaSeries.count { item ->
                item.dateCompleted?.let { timestamp ->
                    val date = LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                    date.year == year && date.monthValue == month
                } ?: false
            }
            monthCounts[month] = bookCount + movieCount + gameCount + mangaCount
        }

        val maxMonth = monthCounts.maxByOrNull { it.value }?.key ?: return MonthCount()
        val maxCount = monthCounts[maxMonth] ?: 0

        if (maxCount == 0) return MonthCount()

        val monthName = LocalDate.of(year, maxMonth, 1)
            .format(DateTimeFormatter.ofPattern("MMMM"))

        return MonthCount(month = monthName, year = year, count = maxCount)
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
        hasPlatinum = hasPlatinum,
        has100Percent = has100Percent,
        createdAt = createdAt
    )
}
