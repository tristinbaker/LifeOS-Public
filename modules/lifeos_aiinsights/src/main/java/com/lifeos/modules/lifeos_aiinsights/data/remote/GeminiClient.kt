package com.lifeos.modules.lifeos_aiinsights.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GeminiClient(
    private val apiKey: String,
    private val okHttpClient: OkHttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val endpoint = "https://api.groq.com/openai/v1/chat/completions"
    private val model = "llama-3.3-70b-versatile"

    suspend fun generateInsights(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        generateWithRetry(prompt, attempt = 1)
    }

    private suspend fun generateWithRetry(prompt: String, attempt: Int): Result<String> {
        val body = json.encodeToString(
            GroqRequest(
                model = model,
                messages = listOf(GroqMessage(role = "user", content = prompt))
            )
        ).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        return try {
            val response = okHttpClient.newCall(request).execute()
            when {
                response.isSuccessful -> {
                    val responseText = response.body?.string() ?: return Result.failure(Exception("Empty response"))
                    val parsed = json.decodeFromString<GroqResponse>(responseText)
                    val text = parsed.choices.firstOrNull()?.message?.content
                        ?: return Result.failure(Exception("No content in response"))
                    Result.success(text)
                }
                response.code == 429 && attempt < 4 -> {
                    val delaySecs = when (attempt) { 1 -> 5L; 2 -> 15L; else -> 30L }
                    delay(delaySecs * 1000)
                    generateWithRetry(prompt, attempt + 1)
                }
                else -> {
                    val errorBody = response.body?.string() ?: "(no body)"
                    Log.e("GroqClient", "HTTP ${response.code}: $errorBody")
                    val msg = if (response.code == 429)
                        "Rate limit reached. Please wait a moment and try again.\n\nDetails: $errorBody"
                    else
                        "API error ${response.code}: $errorBody"
                    Result.failure(Exception(msg))
                }
            }
        } catch (e: Exception) {
            Log.e("GroqClient", "Request failed", e)
            Result.failure(e)
        }
    }
}
