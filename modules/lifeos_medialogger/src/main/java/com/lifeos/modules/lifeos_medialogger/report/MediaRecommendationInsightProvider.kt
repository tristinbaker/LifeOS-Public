package com.lifeos.modules.lifeos_medialogger.report

import com.lifeos.core.InsightProvider
import com.lifeos.modules.lifeos_medialogger.data.local.MediaItemDao
import com.lifeos.modules.lifeos_medialogger.data.local.MediaType
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MediaRecommendationInsightProvider @Inject constructor(
    private val mediaItemDao: MediaItemDao
) : InsightProvider {

    override val insightId = "media_next"
    override val cardTitle = "What to Watch/Read/Play"
    override val cardQuestion = "Based on your ratings and taste, what should you consume next?"

    override suspend fun buildDataContext(): String {
        val movies = mediaItemDao.getItemsByType(MediaType.MOVIE).first()
        val books = mediaItemDao.getItemsByType(MediaType.BOOK).first()
        val games = mediaItemDao.getItemsByType(MediaType.GAME).first()

        val allCompleted = (movies + books + games)
            .filter { it.dateCompleted != null && it.rating != null }
            .sortedByDescending { it.dateCompleted }

        if (allCompleted.isEmpty()) return "No rated media logged yet."

        val topRated = allCompleted.filter { (it.rating ?: 0f) >= 4.0f }.take(20)
        val recent = allCompleted.take(10)

        return buildString {
            appendLine("Logged media (${allCompleted.size} total):")
            appendLine()

            val topMovies = topRated.filter { it.type == MediaType.MOVIE }
            val topBooks = topRated.filter { it.type == MediaType.BOOK }
            val topGames = topRated.filter { it.type == MediaType.GAME }

            if (topMovies.isNotEmpty()) {
                appendLine("Highly-rated movies (4+ stars):")
                topMovies.take(8).forEach { m ->
                    appendLine("  ${m.title} — ${"%.1f".format(m.rating)}/5${m.notes?.let { " (\"${it.take(60)}\")" } ?: ""}")
                }
                appendLine()
            }
            if (topBooks.isNotEmpty()) {
                appendLine("Highly-rated books (4+ stars):")
                topBooks.take(8).forEach { b ->
                    val authorStr = b.author?.let { " by $it" } ?: ""
                    appendLine("  ${b.title}$authorStr — ${"%.1f".format(b.rating)}/5")
                }
                appendLine()
            }
            if (topGames.isNotEmpty()) {
                appendLine("Highly-rated games (4+ stars):")
                topGames.take(8).forEach { g ->
                    val platformStr = g.platform?.let { " ($it)" } ?: ""
                    appendLine("  ${g.title}$platformStr — ${"%.1f".format(g.rating)}/5")
                }
                appendLine()
            }

            if (topRated.isEmpty()) {
                appendLine("Recent logged items (no 4+ rated items yet):")
                recent.forEach { item ->
                    appendLine("  [${item.type.name}] ${item.title} — ${item.rating?.let { "${"%.1f".format(it)}/5" } ?: "unrated"}")
                }
            }

            appendLine("Genre/style preferences can be inferred from the above. Suggest one specific title to try next in each category the user engages with.")
        }.trimEnd()
    }
}
