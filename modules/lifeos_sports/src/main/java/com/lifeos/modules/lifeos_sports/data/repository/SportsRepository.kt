package com.lifeos.modules.lifeos_sports.data.repository

import com.lifeos.modules.lifeos_sports.data.api.EspnApiService
import com.lifeos.modules.lifeos_sports.data.api.EspnEvent
import com.lifeos.modules.lifeos_sports.data.local.FavoriteTeamDao
import com.lifeos.modules.lifeos_sports.data.local.FavoriteTeamEntity
import com.lifeos.modules.lifeos_sports.data.local.SportsCacheDao
import com.lifeos.modules.lifeos_sports.data.local.SportsCacheEntity
import com.lifeos.modules.lifeos_sports.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SportsRepository @Inject constructor(
    private val favoriteTeamDao: FavoriteTeamDao,
    private val sportsCacheDao: SportsCacheDao
) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    fun getFavoriteTeams(): Flow<List<FavoriteTeam>> =
        favoriteTeamDao.getAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun addFavorite(team: FavoriteTeam) = favoriteTeamDao.insert(team.toEntity())

    suspend fun removeFavorite(team: FavoriteTeam) =
        favoriteTeamDao.delete(compositeId(team.league.name, team.id))

    suspend fun isFavorite(team: FavoriteTeam): Boolean =
        favoriteTeamDao.isFavorite(compositeId(team.league.name, team.id))

    private fun compositeId(leagueName: String, teamId: String) = "${leagueName}_${teamId}"

    private fun todayDateString() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // Defensive read: rows written before the compact-cache fix may be too large for CursorWindow.
    private suspend fun safeGet(key: String): SportsCacheEntity? {
        return try {
            sportsCacheDao.get(key)
        } catch (e: Exception) {
            try { sportsCacheDao.deleteByKey(key) } catch (_: Exception) {}
            null
        }
    }

    suspend fun getScoresForLeague(league: League): List<GameScore> {
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val response = EspnApiService.getScoreboardForDate(league, todayStr) ?: return emptyList()
        return response.events.mapNotNull { event ->
            val comp = event.competitions.firstOrNull() ?: return@mapNotNull null
            val home = comp.competitors.firstOrNull { it.homeAway == "home" }
            val away = comp.competitors.firstOrNull { it.homeAway == "away" }
            val status = when (comp.status.type.state) {
                "pre" -> GameStatus.PRE
                "in" -> GameStatus.IN
                else -> GameStatus.POST
            }
            val isPre = status == GameStatus.PRE
            GameScore(
                id = event.id,
                league = league,
                homeTeam = TeamScore(
                    id = home?.team?.id ?: "",
                    name = home?.team?.displayName ?: "",
                    abbreviation = home?.team?.abbreviation ?: "",
                    score = home?.score?.takeIf { it.isNotBlank() },
                    logo = home?.team?.logo ?: "",
                    record = home?.records?.firstOrNull { it.name == "overall" }?.summary,
                    probablePitcher = if (isPre && league == League.MLB)
                        home?.probables?.firstOrNull()?.athlete?.shortName else null,
                    pitcherEra = if (isPre && league == League.MLB)
                        home?.probables?.firstOrNull()?.statistics?.find { it.name == "ERA" }?.displayValue else null
                ),
                awayTeam = TeamScore(
                    id = away?.team?.id ?: "",
                    name = away?.team?.displayName ?: "",
                    abbreviation = away?.team?.abbreviation ?: "",
                    score = away?.score?.takeIf { it.isNotBlank() },
                    logo = away?.team?.logo ?: "",
                    record = away?.records?.firstOrNull { it.name == "overall" }?.summary,
                    probablePitcher = if (isPre && league == League.MLB)
                        away?.probables?.firstOrNull()?.athlete?.shortName else null,
                    pitcherEra = if (isPre && league == League.MLB)
                        away?.probables?.firstOrNull()?.statistics?.find { it.name == "ERA" }?.displayValue else null
                ),
                status = status,
                displayClock = comp.status.displayClock,
                period = comp.status.type.shortDetail,
                date = event.date,
                situation = if (status == GameStatus.IN) mapSituation(comp, league) else null
            )
        }
    }


    private fun mapSituation(comp: com.lifeos.modules.lifeos_sports.data.api.EspnCompetition, league: League): GameSituation? {
        val sit = comp.situation ?: return null
        return when (league) {
            League.MLB -> GameSituation(
                balls = sit.balls,
                strikes = sit.strikes,
                outs = sit.outs,
                onFirst = sit.onFirst,
                onSecond = sit.onSecond,
                onThird = sit.onThird,
                pitcher = sit.pitcher?.athlete?.shortName,
                batter = sit.batter?.athlete?.shortName
            )
            League.NFL, League.NCAA_FOOTBALL -> {
                val possessionTeam = comp.competitors.firstOrNull { it.team.id == sit.possession }
                GameSituation(
                    downDistanceText = sit.downDistanceText.takeIf { it.isNotBlank() }
                        ?: sit.shortDownDistanceText.takeIf { it.isNotBlank() },
                    possession = possessionTeam?.team?.abbreviation,
                    isRedZone = sit.isRedZone
                )
            }
            else -> null
        }
    }

    suspend fun getUpcomingGamesForTeam(
        league: League,
        teamId: String,
        maxGames: Int,
        forceRefresh: Boolean = false
    ): List<GameScore> {
        val today = todayDateString()
        val cacheKey = "schedule_${league.name}_${teamId}"
        if (!forceRefresh) {
            val cached = safeGet(cacheKey)
            if (cached != null && cached.cachedDate == today) {
                return try { json.decodeFromString<List<GameScore>>(cached.data) } catch (e: Exception) { emptyList() }
            }
        }

        val dateFmt = SimpleDateFormat("yyyyMMdd", Locale.US)
        val cal = Calendar.getInstance()
        val maxDays = if (league == League.NFL || league == League.NCAA_FOOTBALL) 28 else 14
        val games = mutableListOf<GameScore>()

        repeat(maxDays) {
            if (games.size >= maxGames) return@repeat
            val dateStr = dateFmt.format(cal.time)
            val response = EspnApiService.getScoreboardForDate(league, dateStr) ?: run {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                return@repeat
            }
            response.events.forEach { event ->
                if (games.size >= maxGames) return@forEach
                val comp = event.competitions.firstOrNull() ?: return@forEach
                if (comp.status.type.state != "pre") return@forEach
                if (!comp.competitors.any { it.team.id == teamId }) return@forEach
                val home = comp.competitors.firstOrNull { it.homeAway == "home" }
                val away = comp.competitors.firstOrNull { it.homeAway == "away" }
                games.add(GameScore(
                    id = event.id,
                    league = league,
                    homeTeam = TeamScore(
                        id = home?.team?.id ?: "",
                        name = home?.team?.displayName ?: "",
                        abbreviation = home?.team?.abbreviation ?: "",
                        score = null,
                        logo = home?.team?.logo ?: ""
                    ),
                    awayTeam = TeamScore(
                        id = away?.team?.id ?: "",
                        name = away?.team?.displayName ?: "",
                        abbreviation = away?.team?.abbreviation ?: "",
                        score = null,
                        logo = away?.team?.logo ?: ""
                    ),
                    status = GameStatus.PRE,
                    displayClock = "",
                    period = comp.status.type.shortDetail,
                    date = event.date
                ))
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        try { sportsCacheDao.upsert(SportsCacheEntity(cacheKey, json.encodeToString(games), today)) } catch (_: Exception) {}
        return games
    }

    suspend fun getStandingsForLeague(league: League, forceRefresh: Boolean = false): List<StandingsGroup> {
        val today = todayDateString()
        val cacheKey = "standings_${league.name}"
        if (!forceRefresh) {
            val cached = safeGet(cacheKey)
            if (cached != null && cached.cachedDate == today) {
                return try { json.decodeFromString<List<StandingsGroup>>(cached.data) } catch (e: Exception) { emptyList() }
            }
        }
        val raw = EspnApiService.getStandingsRaw(league) ?: return emptyList()
        val groups = parseStandingsFromJson(raw)
        if (groups.isNotEmpty()) {
            try { sportsCacheDao.upsert(SportsCacheEntity(cacheKey, json.encodeToString(groups), today)) } catch (_: Exception) {}
        }
        return groups
    }

    private fun parseStandingsFromJson(raw: String): List<StandingsGroup> {
        return try {
            val root = json.parseToJsonElement(raw).jsonObject
            val groups = mutableListOf<StandingsGroup>()
            walkStandingsNode(root, depth = 0, groups = groups)
            groups
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Manual JSON tree-walk for standings — avoids silent failures from field-type mismatches.
    private fun walkStandingsNode(node: JsonObject, depth: Int, groups: MutableList<StandingsGroup>) {
        val name = (node["name"] as? JsonPrimitive)?.contentOrNull ?: ""
        val entries = (node["standings"] as? JsonObject)?.get("entries") as? JsonArray
        val children = node["children"] as? JsonArray

        // Only emit at the most granular level: skip this node if any child also has entries.
        val childrenHaveEntries = children?.any { child ->
            (child as? JsonObject)?.let { c ->
                ((c["standings"] as? JsonObject)?.get("entries") as? JsonArray)?.isNotEmpty() == true
            } == true
        } == true

        if (entries != null && entries.isNotEmpty() && depth > 0 && !childrenHaveEntries) {
            val standings = entries.mapIndexedNotNull { index, entryEl ->
                val entry = entryEl as? JsonObject ?: return@mapIndexedNotNull null
                val team = entry["team"] as? JsonObject
                val stats = entry["stats"] as? JsonArray ?: JsonArray(emptyList())

                fun stat(statName: String) = stats.firstOrNull { el ->
                    ((el as? JsonObject)?.get("name") as? JsonPrimitive)?.contentOrNull == statName
                }?.let { el -> ((el as? JsonObject)?.get("displayValue") as? JsonPrimitive)?.contentOrNull }

                Standing(
                    teamId = (team?.get("id") as? JsonPrimitive)?.contentOrNull ?: "",
                    teamName = (team?.get("displayName") as? JsonPrimitive)?.contentOrNull ?: "",
                    abbreviation = (team?.get("abbreviation") as? JsonPrimitive)?.contentOrNull ?: "",
                    logo = "",
                    wins = stat("wins")?.toIntOrNull() ?: 0,
                    losses = stat("losses")?.toIntOrNull() ?: 0,
                    ties = stat("ties")?.toIntOrNull() ?: 0,
                    winPercent = stat("winPercent") ?: stat("pointPercentage") ?: stat("points") ?: ".000",
                    gamesBack = stat("gamesBehind") ?: "-",
                    lastTen = stat("Last Ten Games") ?: stat("last10") ?: stat("l10") ?: "",
                    rank = index + 1,
                    divisionName = name
                )
            }
            if (standings.isNotEmpty()) groups.add(StandingsGroup(name, standings))
        }

        children?.forEach { child ->
            (child as? JsonObject)?.let { walkStandingsNode(it, depth + 1, groups) }
        }
    }

    suspend fun getTeamsForLeague(league: League): List<FavoriteTeam> {
        val response = EspnApiService.getTeams(league) ?: return emptyList()
        return response.sports.firstOrNull()?.leagues?.firstOrNull()?.teams?.map { entry ->
            FavoriteTeam(
                id = entry.team.id,
                name = entry.team.displayName,
                abbreviation = entry.team.abbreviation,
                logo = entry.team.logo,
                league = league
            )
        } ?: emptyList()
    }

    suspend fun getCachedLastGame(league: League, teamId: String): GameDetails? {
        val cached = safeGet("lastgame_${league.name}_${teamId}") ?: return null
        return try { json.decodeFromString<GameDetails>(cached.data) } catch (e: Exception) { null }
    }

    suspend fun getLastCompletedGame(league: League, teamId: String, forceRefresh: Boolean = false): GameDetails? {
        val today = todayDateString()
        val cacheKey = "lastgame_${league.name}_${teamId}"
        if (!forceRefresh) {
            val cached = safeGet(cacheKey)
            if (cached != null && cached.cachedDate == today) {
                return try { json.decodeFromString<GameDetails>(cached.data) } catch (e: Exception) { null }
            }
        }

        val dateFmt = SimpleDateFormat("yyyyMMdd", Locale.US)
        val cal = Calendar.getInstance()

        repeat(21) { daysBack ->
            if (daysBack > 0) cal.add(Calendar.DAY_OF_YEAR, -1)
            val dateStr = dateFmt.format(cal.time)
            val response = EspnApiService.getScoreboardForDate(league, dateStr) ?: return@repeat
            val event = response.events.firstOrNull { event ->
                val comp = event.competitions.firstOrNull() ?: return@firstOrNull false
                val isCompleted = comp.status.type.state == "post" || comp.status.type.completed
                isCompleted && comp.competitors.any { it.team.id == teamId }
            } ?: return@repeat
            val result = mapEventToGameDetails(event, league)
            try { sportsCacheDao.upsert(SportsCacheEntity(cacheKey, json.encodeToString(result), today)) } catch (_: Exception) {}
            return result
        }
        return null
    }

    private fun mapEventToGameDetails(event: EspnEvent, league: League): GameDetails {
        val comp = event.competitions.first()
        val home = comp.competitors.firstOrNull { it.homeAway == "home" }
        val away = comp.competitors.firstOrNull { it.homeAway == "away" }

        val leaders = comp.competitors.flatMap { competitor ->
            competitor.leaders.flatMap { cat ->
                cat.leaders.take(1).map { leader ->
                    StatLeader(
                        category = statCategoryLabel(cat.name),
                        displayValue = leader.displayValue,
                        athleteName = leader.athlete.displayName,
                        teamAbbreviation = competitor.team.abbreviation
                    )
                }
            }
        }.distinctBy { it.athleteName }.take(6)

        return GameDetails(
            id = event.id,
            league = league,
            homeTeam = TeamScore(
                id = home?.team?.id ?: "",
                name = home?.team?.displayName ?: "",
                abbreviation = home?.team?.abbreviation ?: "",
                score = home?.score?.takeIf { it.isNotBlank() },
                logo = home?.team?.logo ?: "",
                record = home?.records?.firstOrNull { it.name == "overall" }?.summary
            ),
            awayTeam = TeamScore(
                id = away?.team?.id ?: "",
                name = away?.team?.displayName ?: "",
                abbreviation = away?.team?.abbreviation ?: "",
                score = away?.score?.takeIf { it.isNotBlank() },
                logo = away?.team?.logo ?: "",
                record = away?.records?.firstOrNull { it.name == "overall" }?.summary
            ),
            date = event.date,
            venue = buildString {
                if (comp.venue.fullName.isNotBlank()) append(comp.venue.fullName)
                if (comp.venue.city.isNotBlank()) append(", ${comp.venue.city}")
                if (comp.venue.state.isNotBlank()) append(", ${comp.venue.state}")
            },
            attendance = if (comp.attendance > 0) "%,d".format(comp.attendance) else "",
            headlines = comp.headlines.map { it.description }.filter { it.isNotBlank() }.take(3),
            leaders = leaders
        )
    }

    private fun statCategoryLabel(name: String): String = when (name.lowercase()) {
        "batting" -> "Batting"
        "pitching" -> "Pitching"
        "passing" -> "Passing"
        "rushing" -> "Rushing"
        "receiving" -> "Receiving"
        "defense", "defensive" -> "Defense"
        "goals" -> "Goals"
        "assists" -> "Assists"
        "points" -> "Points"
        "saves" -> "Saves"
        "rebounds" -> "Rebounds"
        "blocks" -> "Blocks"
        "steals" -> "Steals"
        else -> name.replaceFirstChar { it.uppercase() }
    }

    private fun FavoriteTeamEntity.toDomain(): FavoriteTeam {
        val league = League.entries.firstOrNull { it.name == leagueName } ?: League.MLB
        return FavoriteTeam(teamId, name, abbreviation, logo, league)
    }

    private fun FavoriteTeam.toEntity() =
        FavoriteTeamEntity(
            compositeId = compositeId(league.name, id),
            teamId = id,
            name = name,
            abbreviation = abbreviation,
            logo = logo,
            leagueName = league.name
        )
}
