package com.lifeos.modules.lifeos_physicalmedia.service

import com.lifeos.modules.lifeos_physicalmedia.data.local.GameSystem
import com.lifeos.modules.lifeos_physicalmedia.domain.model.displayName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceChartingService @Inject constructor() {

    private val client = OkHttpClient.Builder().followRedirects(true).build()
    private val userAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

    suspend fun fetchCibPrice(title: String, system: GameSystem): Double? = withContext(Dispatchers.IO) {
        val slug = system.priceChartingSlug() ?: return@withContext null
        try {
            val query = URLEncoder.encode("$title ${system.displayName()}", "UTF-8")
            val url = "https://www.pricecharting.com/search-products?q=$query&type=prices&exclude-variants=false"
            val html = client.newCall(
                Request.Builder().url(url).header("User-Agent", userAgent).build()
            ).execute().body?.string() ?: return@withContext null

            val doc = Jsoup.parse(html)
            val rows = doc.select("table#games_table tbody tr")

            // Prefer first row whose console link matches our system
            for (row in rows) {
                val consoleHref = row.selectFirst("td.console a")?.attr("href") ?: continue
                if (!consoleHref.contains(slug)) continue
                val text = row.selectFirst("td.cib_price span.js-price")?.text()
                    ?.takeIf { it.isNotBlank() } ?: continue
                return@withContext parseDollar(text)
            }

            // Fallback: take first row's CIB price regardless of console
            rows.firstOrNull()
                ?.selectFirst("td.cib_price span.js-price")
                ?.text()
                ?.takeIf { it.isNotBlank() }
                ?.let { parseDollar(it) }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseDollar(text: String): Double? =
        text.removePrefix("$").replace(",", "").trim().toDoubleOrNull()
}

fun GameSystem.priceChartingSlug(): String? = when (this) {
    GameSystem.NES -> "nes"
    GameSystem.SNES -> "super-nintendo"
    GameSystem.N64 -> "nintendo-64"
    GameSystem.GAMECUBE -> "gamecube"
    GameSystem.WII -> "wii"
    GameSystem.WII_U -> "wii-u"
    GameSystem.SWITCH -> "nintendo-switch"
    GameSystem.SWITCH_2 -> "nintendo-switch-2"
    GameSystem.GAME_BOY -> "game-boy"
    GameSystem.GAME_BOY_COLOR -> "game-boy-color"
    GameSystem.GAME_BOY_ADVANCE -> "game-boy-advance"
    GameSystem.DS -> "nintendo-ds"
    GameSystem.THREE_DS -> "nintendo-3ds"
    GameSystem.PS1 -> "playstation"
    GameSystem.PS2 -> "playstation-2"
    GameSystem.PS3 -> "playstation-3"
    GameSystem.PS4 -> "playstation-4"
    GameSystem.PS5 -> "playstation-5"
    GameSystem.PSP -> "psp"
    GameSystem.PS_VITA -> "ps-vita"
    GameSystem.XBOX -> "xbox"
    GameSystem.XBOX_360 -> "xbox-360"
    GameSystem.XBOX_ONE -> "xbox-one"
    GameSystem.XBOX_SERIES_X_S -> "xbox-series-x"
    GameSystem.PC, GameSystem.OTHER -> null
}
