package com.lifeos.modules.lifeos_mealtracker.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_mealtracker.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class ShameState(
    val message: String,
    val photoUri: String?,
    val calorieExcess: Int,
    val forced: Boolean = false,
    val countdownSeconds: Int = 10
)

data class MotivationState(
    val message: String,
    val streak: Int
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _shameState = MutableStateFlow<ShameState?>(null)
    val shameState: StateFlow<ShameState?> = _shameState.asStateFlow()

    private val _motivationState = MutableStateFlow<MotivationState?>(null)
    val motivationState: StateFlow<MotivationState?> = _motivationState.asStateFlow()

    fun updateDailyCalorieGoal(goal: Int) {
        viewModelScope.launch {
            settingsRepository.updateDailyCalorieGoal(goal)
        }
    }

    fun updateGoalWeight(goal: Double?) {
        viewModelScope.launch {
            settingsRepository.updateGoalWeight(goal)
        }
    }

    fun updateWeeklyWeightGoalRate(rate: Double) {
        viewModelScope.launch {
            settingsRepository.updateWeeklyWeightGoalRate(rate)
        }
    }

    fun updateMacroGoals(protein: Int?, carbs: Int?, fat: Int?) {
        viewModelScope.launch {
            settingsRepository.updateMacroGoals(protein, carbs, fat)
        }
    }

    fun updateShamePhotoUri(uri: Uri?) {
        viewModelScope.launch {
            settingsRepository.updateShamePhotoUri(uri?.toString())
        }
    }

    fun updateShameMessages(messages: List<String>) {
        viewModelScope.launch {
            settingsRepository.updateShameMessages(messages)
        }
    }

    fun updateMotivationalMessages(messages: List<String>) {
        viewModelScope.launch {
            settingsRepository.updateMotivationalMessages(messages)
        }
    }

    fun updateDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDarkMode(isDark)
        }
    }

    fun updateHourlyShameNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateHourlyShameNotifications(enabled)
        }
    }

    fun updateMotivationalMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateMotivationalMode(enabled)
        }
    }

    fun updateStreak(newStreak: Int) {
        viewModelScope.launch {
            settingsRepository.updateCurrentStreak(newStreak)
        }
    }

    fun incrementStreak() {
        viewModelScope.launch {
            val newStreak = settings.value.currentStreak + 1
            settingsRepository.updateCurrentStreak(newStreak)
        }
    }

    fun resetStreak() {
        viewModelScope.launch {
            settingsRepository.updateCurrentStreak(0)
        }
    }

    fun showShameOverlay(calorieExcess: Int, forced: Boolean = false) {
        val currentSettings = settings.value
        val streak = currentSettings.currentStreak
        
        val message: String
        val countdown: Int

        if (streak >= 7 && forced) {
            val streakBreakingMessages = extraShameOnStreakBreakMessages
            message = streakBreakingMessages[Random.nextInt(streakBreakingMessages.size)]
                .replace("[X]", streak.toString())
            countdown = 30
        } else {
            val messages = currentSettings.shameMessages
            message = messages[Random.nextInt(messages.size)]
                .replace("[X]", calorieExcess.toString())
                .replace("[Y]", (calorieExcess / 10).toString())
            countdown = if (calorieExcess > currentSettings.dailyCalorieGoal) 20 else 10
        }

        _shameState.value = ShameState(
            message = message,
            photoUri = currentSettings.shamePhotoUri,
            calorieExcess = calorieExcess,
            forced = forced,
            countdownSeconds = countdown
        )
    }

    fun showMotivation(streak: Int) {
        val messages = settings.value.motivationalMessages
        if (messages.isNotEmpty()) {
            val message = messages[Random.nextInt(messages.size)]
                .replace("[X]", streak.toString())
            _motivationState.value = MotivationState(message, streak)
        }
    }

    fun dismissShame() {
        _shameState.value = null
    }

    fun dismissMotivation() {
        _motivationState.value = null
    }
}
