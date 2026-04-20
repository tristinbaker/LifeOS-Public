package com.lifeos.modules.lifeos_sports.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class League(
    val displayName: String,
    val sport: String,
    val league: String
) {
    MLB("MLB", "baseball", "mlb"),
    NBA("NBA", "basketball", "nba"),
    NHL("NHL", "hockey", "nhl"),
    NFL("NFL", "football", "nfl"),
    NCAA_FOOTBALL("NCAA Football", "football", "college-football")
}
