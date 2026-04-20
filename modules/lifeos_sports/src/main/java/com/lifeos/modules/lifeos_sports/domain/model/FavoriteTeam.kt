package com.lifeos.modules.lifeos_sports.domain.model

data class FavoriteTeam(
    val id: String,
    val name: String,
    val abbreviation: String,
    val logo: String,
    val league: League
)
