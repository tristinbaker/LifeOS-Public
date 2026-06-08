package com.lifeos.modules.lifeos_physicalmedia.report

import com.lifeos.core.InsightProvider
import com.lifeos.modules.lifeos_physicalmedia.data.local.PhysicalMovieDao
import com.lifeos.modules.lifeos_physicalmedia.data.local.PhysicalTvSeriesDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CollectionPickInsightProvider @Inject constructor(
    private val movieDao: PhysicalMovieDao,
    private val tvSeriesDao: PhysicalTvSeriesDao
) : InsightProvider {

    override val insightId = "collection_pick"
    override val cardTitle = "Tonight's Pick"
    override val cardQuestion = "What should you watch from your physical collection tonight?"

    override suspend fun buildDataContext(): String {
        val movies = movieDao.getAll().first()
        val tvSeries = tvSeriesDao.getAll().first()

        if (movies.isEmpty() && tvSeries.isEmpty()) return "No physical movie or TV series in your collection."

        val shuffledMovies = movies.shuffled().take(25)
        val shuffledTv = tvSeries.shuffled().take(10)

        return buildString {
            appendLine("Physical media collection (sample for tonight's pick):")
            appendLine()

            if (shuffledMovies.isNotEmpty()) {
                appendLine("Movies available (${movies.size} total, showing ${shuffledMovies.size}):")
                shuffledMovies.forEach { m ->
                    val extras = buildList {
                        if (m.steelbook) add("steelbook")
                        if (m.limitedEdition) add("limited edition")
                        m.boutiqueLabel?.let { add(it) }
                    }.joinToString(", ")
                    val extrasStr = if (extras.isNotBlank()) " [$extras]" else ""
                    appendLine("  ${m.title} (${m.format.name.replace("_", " ")})$extrasStr")
                }
                appendLine()
            }

            if (shuffledTv.isNotEmpty()) {
                appendLine("TV Series available (${tvSeries.size} total, showing ${shuffledTv.size}):")
                shuffledTv.forEach { s ->
                    val completeStr = if (s.completeSeries) " [Complete Series]" else ""
                    appendLine("  ${s.title} (${s.format.name.replace("_", " ")})$completeStr")
                }
            }

            appendLine()
            appendLine("Pick one specific title from the above list and explain briefly why it's a great choice for tonight.")
        }.trimEnd()
    }
}
