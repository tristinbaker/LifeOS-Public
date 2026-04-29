package com.lifeos.modules.lifeos_sports.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EspnScoreboardResponse(
    val events: List<EspnEvent> = emptyList()
)

@Serializable
data class EspnEvent(
    val id: String = "",
    val date: String = "",
    val name: String = "",
    val competitions: List<EspnCompetition> = emptyList()
)

@Serializable
data class EspnCompetition(
    val id: String = "",
    val competitors: List<EspnCompetitor> = emptyList(),
    val status: EspnStatus = EspnStatus(),
    val venue: EspnVenue = EspnVenue(),
    val attendance: Int = 0,
    val headlines: List<EspnHeadline> = emptyList(),
    val situation: EspnSituation? = null
)

@Serializable
data class EspnSituation(
    val balls: Int = 0,
    val strikes: Int = 0,
    val outs: Int = 0,
    val onFirst: Boolean = false,
    val onSecond: Boolean = false,
    val onThird: Boolean = false,
    val down: Int = 0,
    val distance: Int = 0,
    val downDistanceText: String = "",
    val shortDownDistanceText: String = "",
    val isRedZone: Boolean = false,
    val possession: String = "",
    val pitcher: EspnProbable? = null,
    val batter: EspnProbable? = null
)

@Serializable
data class EspnCompetitor(
    val id: String = "",
    val homeAway: String = "",
    val score: String = "",
    val team: EspnTeam = EspnTeam(),
    val records: List<EspnRecord> = emptyList(),
    val leaders: List<EspnLeaderCategory> = emptyList(),
    val probables: List<EspnProbable> = emptyList()
)

@Serializable
data class EspnProbable(
    val athlete: EspnAthlete = EspnAthlete(),
    val statistics: List<EspnStat> = emptyList()
)

@Serializable
data class EspnRecord(
    val name: String = "",
    val type: String = "",
    val summary: String = ""
)

@Serializable
data class EspnTeam(
    val id: String = "",
    val displayName: String = "",
    val abbreviation: String = "",
    val logo: String = "",
    val name: String = ""
)

@Serializable
data class EspnStatus(
    val displayClock: String = "",
    val period: Int = 0,
    val type: EspnStatusType = EspnStatusType()
)

@Serializable
data class EspnStatusType(
    val state: String = "",
    val completed: Boolean = false,
    val description: String = "",
    val shortDetail: String = ""
)

@Serializable
data class EspnVenue(
    val fullName: String = "",
    val city: String = "",
    val state: String = ""
)

@Serializable
data class EspnHeadline(
    val description: String = "",
    val shortLinkText: String = ""
)

@Serializable
data class EspnLeaderCategory(
    val name: String = "",
    val displayName: String = "",
    val leaders: List<EspnLeader> = emptyList()
)

@Serializable
data class EspnLeader(
    val displayValue: String = "",
    val athlete: EspnAthlete = EspnAthlete(),
    val team: EspnTeam = EspnTeam()
)

@Serializable
data class EspnAthlete(
    val displayName: String = "",
    val shortName: String = ""
)

@Serializable
data class EspnStandingsResponse(
    val children: List<EspnStandingsGroup> = emptyList(),
    val standings: EspnStandingsTable? = null
)

@Serializable
data class EspnStandingsGroup(
    val name: String = "",
    val abbreviation: String = "",
    val standings: EspnStandingsTable = EspnStandingsTable(),
    val children: List<EspnStandingsGroup> = emptyList()
)

@Serializable
data class EspnStandingsTable(
    val entries: List<EspnStandingsEntry> = emptyList()
)

@Serializable
data class EspnStandingsEntry(
    val team: EspnTeam = EspnTeam(),
    val stats: List<EspnStat> = emptyList()
)

@Serializable
data class EspnStat(
    val name: String = "",
    val displayName: String = "",
    val displayValue: String = "",
    val value: Double = 0.0
)

@Serializable
data class EspnTeamsResponse(
    val sports: List<EspnSport> = emptyList()
)

@Serializable
data class EspnSport(
    val leagues: List<EspnLeagueData> = emptyList()
)

@Serializable
data class EspnLeagueData(
    val teams: List<EspnTeamEntry> = emptyList()
)

@Serializable
data class EspnTeamEntry(
    val team: EspnTeam = EspnTeam()
)

@Serializable
data class EspnScheduleResponse(
    val team: EspnTeam = EspnTeam(),
    val events: List<EspnScheduleEvent> = emptyList()
)

@Serializable
data class EspnScheduleEvent(
    val id: String = "",
    val date: String = "",
    val name: String = "",
    val competitions: List<EspnCompetition> = emptyList()
)
