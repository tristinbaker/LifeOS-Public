package com.lifeos.modules.lifeos_mealtracker.data.remote

import com.lifeos.modules.lifeos_mealtracker.domain.model.FatSecretServing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

class FatSecretApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val clientId: String,
    private val clientSecret: String
) {
    private val tokenUrl = "https://oauth.fatsecret.com/connect/token"
    private val baseUrl = "https://platform.fatsecret.com/rest/server.api"

    @Volatile private var cachedToken: String? = null
    @Volatile private var tokenExpiresAt: Long = 0L
    private val tokenMutex = Mutex()

    private suspend fun getToken(): String = tokenMutex.withLock {
        val now = System.currentTimeMillis()
        if (cachedToken != null && now < tokenExpiresAt - 30_000) {
            return@withLock cachedToken!!
        }
        withContext(Dispatchers.IO) {
            val body = FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("scope", "basic barcode")
                .build()
            val request = Request.Builder().url(tokenUrl).post(body).build()
            val responseBody = okHttpClient.newCall(request).execute().use { it.body?.string() }
                ?: throw Exception("Empty token response")
            val json = JSONObject(responseBody)
            if (json.has("error")) throw Exception("Token error: ${json.optString("error_description")}")
            cachedToken = json.getString("access_token")
            val expiresIn = json.optLong("expires_in", 86400)
            tokenExpiresAt = now + expiresIn * 1000
            cachedToken!!
        }
    }

    /** Returns null if barcode not found in FatSecret. Throws on network error. */
    suspend fun getFoodIdForBarcode(barcode: String): String? = withContext(Dispatchers.IO) {
        val token = getToken()
        val url = "$baseUrl?method=food.find_id_for_barcode&barcode=$barcode&format=json"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .build()
        val responseBody = okHttpClient.newCall(request).execute().use { it.body?.string() }
            ?: throw Exception("Empty barcode response")
        val json = JSONObject(responseBody)
        if (json.has("error")) return@withContext null
        val foodId = json.optJSONObject("food_id")?.optString("value")
        if (foodId.isNullOrEmpty() || foodId == "0") null else foodId
    }

    /**
     * Returns food name + serving list. Handles FatSecret quirk where "serving" is a JSONObject
     * (single result) vs JSONArray (multiple results).
     */
    suspend fun getServingsForFood(foodId: String): Pair<String, List<FatSecretServing>> =
        withContext(Dispatchers.IO) {
            val token = getToken()
            val url = "$baseUrl?method=food.get.v2&food_id=$foodId&format=json"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .build()
            val responseBody = okHttpClient.newCall(request).execute().use { it.body?.string() }
                ?: throw Exception("Empty food response")
            val json = JSONObject(responseBody)
            val food = json.optJSONObject("food") ?: return@withContext Pair("Unknown Food", emptyList())
            val foodName = food.optString("food_name", "Unknown Food")
            val servingsObj = food.optJSONObject("servings") ?: return@withContext Pair(foodName, emptyList())

            val servingArray = servingsObj.optJSONArray("serving")
            val servingObj = servingsObj.optJSONObject("serving")

            val servingJsonList = when {
                servingArray != null -> (0 until servingArray.length()).map { servingArray.getJSONObject(it) }
                servingObj != null -> listOf(servingObj)
                else -> emptyList()
            }

            val servings = servingJsonList.map { s ->
                FatSecretServing(
                    description = s.optString("serving_description", "1 serving"),
                    calories = s.optString("calories", "0").toIntOrNull() ?: 0,
                    protein = s.optString("protein", "0").toDoubleOrNull()?.toInt() ?: 0,
                    carbs = s.optString("carbohydrate", "0").toDoubleOrNull()?.toInt() ?: 0,
                    fat = s.optString("fat", "0").toDoubleOrNull()?.toInt() ?: 0
                )
            }
            Pair(foodName, servings)
        }
}
