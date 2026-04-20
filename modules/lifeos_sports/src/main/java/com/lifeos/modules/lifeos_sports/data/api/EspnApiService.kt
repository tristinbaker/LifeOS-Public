package com.lifeos.modules.lifeos_sports.data.api

import com.lifeos.modules.lifeos_sports.domain.model.League
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URL

object EspnApiService {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private const val BASE = "https://site.api.espn.com/apis/site/v2/sports"

    suspend fun getScoreboard(league: League): EspnScoreboardResponse? =
        withContext(Dispatchers.IO) {
            try {
                val url = "$BASE/${league.sport}/${league.league}/scoreboard"
                json.decodeFromString<EspnScoreboardResponse>(URL(url).readText())
            } catch (e: Exception) {
                null
            }
        }

    suspend fun getScoreboardForDate(league: League, yyyymmdd: String): EspnScoreboardResponse? =
        withContext(Dispatchers.IO) {
            try {
                val url = "$BASE/${league.sport}/${league.league}/scoreboard?dates=$yyyymmdd"
                json.decodeFromString<EspnScoreboardResponse>(URL(url).readText())
            } catch (e: Exception) {
                null
            }
        }

    suspend fun getStandingsRaw(league: League): String? =
        withContext(Dispatchers.IO) {
            try {
                val url = "https://site.web.api.espn.com/apis/v2/sports/${league.sport}/${league.league}/standings?region=us&lang=en&level=3"
                URL(url).readText()
            } catch (e: Exception) {
                null
            }
        }

    suspend fun getTeams(league: League): EspnTeamsResponse? =
        withContext(Dispatchers.IO) {
            try {
                // College football has 900+ teams; high limit ensures FBS teams like Tennessee (ID 2633) are included
                val limit = if (league == League.NCAA_FOOTBALL) 1000 else 200
                val url = "$BASE/${league.sport}/${league.league}/teams?limit=$limit"
                json.decodeFromString<EspnTeamsResponse>(URL(url).readText())
            } catch (e: Exception) {
                null
            }
        }
}
