package com.tristinbaker.lifeos

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

object UserPreferences {
    private val USER_NAME = stringPreferencesKey("user_name")
    private val CITY_NAME = stringPreferencesKey("city_name")
    private val LATITUDE = doublePreferencesKey("latitude")
    private val LONGITUDE = doublePreferencesKey("longitude")
    private val SHOW_WEATHER = booleanPreferencesKey("show_weather")
    private val GRID_COLUMNS = intPreferencesKey("grid_columns")

    fun userName(context: Context): Flow<String> =
        context.userDataStore.data.map { it[USER_NAME] ?: "" }

    fun cityName(context: Context): Flow<String> =
        context.userDataStore.data.map { it[CITY_NAME] ?: "" }

    fun latitude(context: Context): Flow<Double?> =
        context.userDataStore.data.map { it[LATITUDE] }

    fun longitude(context: Context): Flow<Double?> =
        context.userDataStore.data.map { it[LONGITUDE] }

    fun showWeather(context: Context): Flow<Boolean> =
        context.userDataStore.data.map { it[SHOW_WEATHER] ?: false }

    fun gridColumns(context: Context): Flow<Int> =
        context.userDataStore.data.map { it[GRID_COLUMNS] ?: 2 }

    suspend fun setUserName(context: Context, name: String) {
        context.userDataStore.edit { it[USER_NAME] = name }
    }

    suspend fun setLocation(context: Context, cityName: String, latitude: Double, longitude: Double) {
        context.userDataStore.edit {
            it[CITY_NAME] = cityName
            it[LATITUDE] = latitude
            it[LONGITUDE] = longitude
        }
    }

    suspend fun setShowWeather(context: Context, show: Boolean) {
        context.userDataStore.edit { it[SHOW_WEATHER] = show }
    }

    suspend fun setGridColumns(context: Context, columns: Int) {
        context.userDataStore.edit { it[GRID_COLUMNS] = columns }
    }
}
