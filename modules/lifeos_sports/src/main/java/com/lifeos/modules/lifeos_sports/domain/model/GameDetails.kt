package com.lifeos.modules.lifeos_sports.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameDetails(
    val id: String,
    val league: League,
    val homeTeam: TeamScore,
    val awayTeam: TeamScore,
    val date: String,
    val venue: String,
    val attendance: String,
    val headlines: List<String>,
    val leaders: List<StatLeader>
)

@Serializable
data class StatLeader(
    val category: String,
    val displayValue: String,
    val athleteName: String,
    val teamAbbreviation: String
)
