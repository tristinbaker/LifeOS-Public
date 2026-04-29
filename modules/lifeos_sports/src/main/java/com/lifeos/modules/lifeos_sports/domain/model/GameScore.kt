package com.lifeos.modules.lifeos_sports.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameScore(
    val id: String,
    val league: League,
    val homeTeam: TeamScore,
    val awayTeam: TeamScore,
    val status: GameStatus,
    val displayClock: String,
    val period: String,
    val date: String,
    val situation: GameSituation? = null
)

@Serializable
data class TeamScore(
    val id: String,
    val name: String,
    val abbreviation: String,
    val score: String?,
    val logo: String,
    val record: String? = null,
    val probablePitcher: String? = null,
    val pitcherEra: String? = null
)

@Serializable
enum class GameStatus {
    PRE, IN, POST
}

@Serializable
data class GameSituation(
    // Baseball
    val balls: Int? = null,
    val strikes: Int? = null,
    val outs: Int? = null,
    val onFirst: Boolean = false,
    val onSecond: Boolean = false,
    val onThird: Boolean = false,
    // Football
    val downDistanceText: String? = null,
    val possession: String? = null,
    val isRedZone: Boolean = false,
    // Baseball live
    val pitcher: String? = null,
    val batter: String? = null
)
