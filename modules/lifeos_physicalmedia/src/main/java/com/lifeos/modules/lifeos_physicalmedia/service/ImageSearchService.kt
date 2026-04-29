package com.lifeos.modules.lifeos_physicalmedia.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

data class ImageSearchResult(
    val thumbnail: String,
    val imageUrl: String,
    val title: String
)

@Singleton
class ImageSearchService @Inject constructor() {

    private val client = OkHttpClient.Builder().followRedirects(true).build()
    private val userAgent = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    suspend fun search(query: String): List<ImageSearchResult> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")

            val html = client.newCall(
                Request.Builder()
                    .url("https://duckduckgo.com/?q=$encoded&iax=images&ia=images")
                    .header("User-Agent", userAgent)
                    .build()
            ).execute().body?.string() ?: return@withContext emptyList()

            val vqd = Regex("""vqd=['"]([^'"]+)['"]""").find(html)?.groupValues?.get(1)
                ?: return@withContext emptyList()

            val json = client.newCall(
                Request.Builder()
                    .url("https://duckduckgo.com/i.js?q=$encoded&vqd=$vqd&f=,,,,,&p=1")
                    .header("User-Agent", userAgent)
                    .header("Referer", "https://duckduckgo.com/")
                    .build()
            ).execute().body?.string() ?: return@withContext emptyList()

            val results = JSONObject(json).getJSONArray("results")
            (0 until minOf(results.length(), 30)).mapNotNull { i ->
                val obj = results.getJSONObject(i)
                ImageSearchResult(
                    thumbnail = obj.optString("thumbnail").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
                    imageUrl = obj.optString("image").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
                    title = obj.optString("title")
                )
            }
        } catch (e: Exception) {
            println("ImageSearchService: ${e.message}")
            emptyList()
        }
    }
}
