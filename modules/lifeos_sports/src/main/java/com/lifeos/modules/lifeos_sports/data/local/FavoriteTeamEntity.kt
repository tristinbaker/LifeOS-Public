package com.lifeos.modules.lifeos_sports.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sports_favorite_teams")
data class FavoriteTeamEntity(
    // Composite key prevents ESPN ID collisions across leagues (e.g. MLB "2" vs NHL "2")
    @PrimaryKey val compositeId: String,
    val teamId: String,
    val name: String,
    val abbreviation: String,
    val logo: String,
    val leagueName: String
)
