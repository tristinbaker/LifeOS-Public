package com.lifeos.modules.lifeos_physicalmedia.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BookFormat { HARDCOVER, SOFTCOVER, LEATHERBACK }

enum class MovieFormat { DVD, VHS, BLU_RAY, BLU_RAY_4K }

enum class GameSystem {
    // Nintendo
    NES, SNES, N64, GAMECUBE, WII, WII_U, SWITCH, SWITCH_2,
    GAME_BOY, GAME_BOY_COLOR, GAME_BOY_ADVANCE, DS, THREE_DS,
    // Sony
    PS1, PS2, PS3, PS4, PS5, PSP, PS_VITA,
    // Microsoft
    XBOX, XBOX_360, XBOX_ONE, XBOX_SERIES_X_S,
    // Other
    PC, OTHER
}

@Entity(tableName = "physical_books")
data class PhysicalBookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val format: BookFormat,
    val coverUrl: String? = null,
    val coverLocalPath: String? = null,
    val seriesName: String? = null,
    val seriesNumber: Float? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "physical_movies")
data class PhysicalMovieEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val format: MovieFormat,
    val limitedEdition: Boolean = false,
    val steelbook: Boolean = false,
    val slipcover: Boolean = false,
    val boutiqueLabel: String? = null,
    val catalogNumber: String? = null,
    val coverUrl: String? = null,
    val coverLocalPath: String? = null,
    val isCollection: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "physical_movie_collection_items",
    foreignKeys = [ForeignKey(
        entity = PhysicalMovieEntity::class,
        parentColumns = ["id"],
        childColumns = ["movieId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("movieId")]
)
data class PhysicalMovieCollectionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val movieId: Long,
    val title: String
)

@Entity(tableName = "physical_games")
data class PhysicalGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val system: GameSystem,
    val coverUrl: String? = null,
    val coverLocalPath: String? = null,
    val isCollection: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "physical_game_collection_items",
    foreignKeys = [ForeignKey(
        entity = PhysicalGameEntity::class,
        parentColumns = ["id"],
        childColumns = ["gameId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("gameId")]
)
data class PhysicalGameCollectionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Long,
    val title: String
)

@Entity(tableName = "physical_tv_series")
data class PhysicalTvSeriesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val format: MovieFormat,
    val completeSeries: Boolean = false,
    val seriesName: String? = null,
    val seriesNumber: Float? = null,
    val coverUrl: String? = null,
    val coverLocalPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
