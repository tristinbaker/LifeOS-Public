package com.lifeos.modules.lifeos_physicalmedia.domain.model

import com.lifeos.modules.lifeos_physicalmedia.data.local.BookFormat
import com.lifeos.modules.lifeos_physicalmedia.data.local.GameSystem
import com.lifeos.modules.lifeos_physicalmedia.data.local.MovieFormat

fun BookFormat.displayName(): String = when (this) {
    BookFormat.HARDCOVER -> "Hardcover"
    BookFormat.SOFTCOVER -> "Softcover"
    BookFormat.LEATHERBACK -> "Leatherback"
}

fun MovieFormat.displayName(): String = when (this) {
    MovieFormat.DVD -> "DVD"
    MovieFormat.VHS -> "VHS"
    MovieFormat.BLU_RAY -> "Blu-Ray"
    MovieFormat.BLU_RAY_4K -> "4K Blu-Ray"
}

fun GameSystem.displayName(): String = when (this) {
    GameSystem.NES -> "NES"
    GameSystem.SNES -> "SNES"
    GameSystem.N64 -> "Nintendo 64"
    GameSystem.GAMECUBE -> "GameCube"
    GameSystem.WII -> "Wii"
    GameSystem.WII_U -> "Wii U"
    GameSystem.SWITCH -> "Nintendo Switch"
    GameSystem.SWITCH_2 -> "Nintendo Switch 2"
    GameSystem.GAME_BOY -> "Game Boy"
    GameSystem.GAME_BOY_COLOR -> "Game Boy Color"
    GameSystem.GAME_BOY_ADVANCE -> "Game Boy Advance"
    GameSystem.DS -> "Nintendo DS"
    GameSystem.THREE_DS -> "Nintendo 3DS"
    GameSystem.PS1 -> "PlayStation"
    GameSystem.PS2 -> "PlayStation 2"
    GameSystem.PS3 -> "PlayStation 3"
    GameSystem.PS4 -> "PlayStation 4"
    GameSystem.PS5 -> "PlayStation 5"
    GameSystem.PSP -> "PSP"
    GameSystem.PS_VITA -> "PS Vita"
    GameSystem.XBOX -> "Xbox"
    GameSystem.XBOX_360 -> "Xbox 360"
    GameSystem.XBOX_ONE -> "Xbox One"
    GameSystem.XBOX_SERIES_X_S -> "Xbox Series X/S"
    GameSystem.PC -> "PC"
    GameSystem.OTHER -> "Other"
}

fun gameSystemGroups(): List<Pair<String, List<GameSystem>>> = listOf(
    "Nintendo" to listOf(
        GameSystem.NES, GameSystem.SNES, GameSystem.N64, GameSystem.GAMECUBE,
        GameSystem.WII, GameSystem.WII_U, GameSystem.SWITCH, GameSystem.SWITCH_2,
        GameSystem.GAME_BOY, GameSystem.GAME_BOY_COLOR, GameSystem.GAME_BOY_ADVANCE,
        GameSystem.DS, GameSystem.THREE_DS
    ),
    "Sony" to listOf(
        GameSystem.PS1, GameSystem.PS2, GameSystem.PS3, GameSystem.PS4,
        GameSystem.PS5, GameSystem.PSP, GameSystem.PS_VITA
    ),
    "Microsoft" to listOf(
        GameSystem.XBOX, GameSystem.XBOX_360, GameSystem.XBOX_ONE, GameSystem.XBOX_SERIES_X_S
    ),
    "Other" to listOf(GameSystem.PC, GameSystem.OTHER)
)

fun bookSearchQuery(title: String): String = "$title book cover"
fun movieSearchQuery(title: String, format: MovieFormat): String = "$title ${format.displayName()} movie cover"
fun gameSearchQuery(title: String, system: GameSystem): String = "$title ${system.displayName()} box art"
