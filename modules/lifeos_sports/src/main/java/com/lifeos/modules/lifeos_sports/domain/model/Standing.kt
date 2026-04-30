package com.lifeos.modules.lifeos_sports.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Standing(
    val teamId: String,
    val teamName: String,
    val abbreviation: String,
    val logo: String,
    val wins: Int,
    val losses: Int,
    val ties: Int,
    val winPercent: String,
    val gamesBack: String,
    val lastTen: String = "",
    val rank: Int,
    val divisionName: String
)

@Serializable
data class StandingsGroup(
    val groupName: String,
    val standings: List<Standing>
)
