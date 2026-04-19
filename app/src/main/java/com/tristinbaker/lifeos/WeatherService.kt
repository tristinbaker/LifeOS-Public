package com.tristinbaker.lifeos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

data class WeatherData(
    val condition: String,
    val conditionPhrase: String,
    val temperature: Int,
    val highTemp: Int,
    val lowTemp: Int,
    val willRain: Boolean,
    val cityName: String,
    val weatherCode: Int,
    val airQuality: Int? = null,
    val airQualityLabel: String? = null
)

object WeatherService {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun geocodeCity(cityName: String): Triple<String, Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val encodedName = cityName.replace(" ", "%20")
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$encodedName&count=1"
            val response = URL(url).readText()
            val geoResponse = json.decodeFromString<GeoResponse>(response)
            
            geoResponse.results?.firstOrNull()?.let { result ->
                Triple(result.name, result.latitude, result.longitude)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getWeather(latitude: Double, longitude: Double): WeatherData? = withContext(Dispatchers.IO) {
        try {
            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current_weather=true&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,weathercode&temperature_unit=fahrenheit&timezone=auto&forecast_days=1"
            val weatherResponse = URL(weatherUrl).readText()
            
            val currentWeatherMatch = Regex("\"current_weather\":\\{[^}]*\"temperature\":(-?\\d+\\.?\\d*)[^}]*\"weathercode\":(\\d+)").find(weatherResponse)
            val dailyWeathercodeMatch = Regex("\"daily\":\\{[^}]*\"weathercode\":\\[(\\d+)").find(weatherResponse)
            val highTempMatch = Regex("\"temperature_2m_max\":\\[(-?\\d+)").find(weatherResponse)
            val lowTempMatch = Regex("\"temperature_2m_min\":\\[(-?\\d+)").find(weatherResponse)
            val precipMatch = Regex("\"precipitation_probability_max\":\\[(\\d+)").find(weatherResponse)
            
            var airQuality: Int? = null
            var airQualityLabel: String? = null
            try {
                val aqUrl = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$latitude&longitude=$longitude&current=european_aqi"
                val aqResponse = URL(aqUrl).readText()
                val aqMatch = Regex("\"european_aqi\":(\\d+)").find(aqResponse)
                airQuality = aqMatch?.groupValues?.get(1)?.toIntOrNull()
                airQualityLabel = when {
                    airQuality != null && airQuality <= 20 -> "Good"
                    airQuality != null && airQuality <= 40 -> "Fair"
                    airQuality != null && airQuality <= 60 -> "Moderate"
                    airQuality != null && airQuality <= 80 -> "Poor"
                    airQuality != null && airQuality <= 100 -> "Very Poor"
                    airQuality != null -> "Extremely Poor"
                    else -> null
                }
            } catch (e: Exception) {
                // Air quality API failed, we'll show "AQ: N/A"
            }
            
            if (currentWeatherMatch != null && highTempMatch != null && lowTempMatch != null) {
                val temperature = currentWeatherMatch.groupValues[1].toDouble().toInt()
                val weathercode = currentWeatherMatch.groupValues[2].toInt()
                val condition = getConditionFromCode(weathercode)
                val conditionPhrase = getConditionPhraseFromCode(weathercode)
                val highTemp = highTempMatch.groupValues[1].toInt()
                val lowTemp = lowTempMatch.groupValues[1].toInt()
                val precipProb = precipMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val willRain = precipProb >= 30 && (weathercode >= 51 || isRainyCode(weathercode))

                WeatherData(condition, conditionPhrase, temperature, highTemp, lowTemp, willRain, "", weathercode, airQuality, airQualityLabel)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isRainyCode(code: Int): Boolean {
        return code in listOf(61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99)
    }

    private fun getConditionPhraseFromCode(code: Int): String {
        return when (code) {
            0 -> "clear"
            1, 2, 3 -> "partly cloudy"
            45, 48 -> "foggy"
            51, 53, 55 -> "drizzling"
            56, 57 -> "freezing"
            61, 63, 65 -> "raining"
            66, 67 -> "freezing"
            71, 73, 75 -> "snowing"
            77 -> "snowing lightly"
            80, 81, 82 -> "showery"
            85, 86 -> "snowing"
            95 -> "stormy"
            96, 99 -> "stormy with hail"
            else -> "unknown"
        }
    }

    private fun getConditionFromCode(code: Int): String {
        return when (code) {
            0 -> "Clear"
            1, 2, 3 -> "Partly cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing rain"
            71, 73, 75 -> "Snow"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown"
        }
    }
}

@Serializable
data class GeoResponse(val results: List<GeoResult>?)

@Serializable
data class GeoResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val admin1: String? = null,
    val country: String? = null
)
