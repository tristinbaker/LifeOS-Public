package com.lifeos.modules.lifeos_mealtracker.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_mealtracker.data.repository.MealRepository
import com.lifeos.modules.lifeos_mealtracker.data.repository.SettingsRepository
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class DayData(
    val date: LocalDate,
    val calories: Int,
    val goal: Int
)

data class AnalyticsUiState(
    val weekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val weekData: List<DayData> = emptyList(),
    val avgCalories: Int = 0,
    val daysOverGoal: Int = 0,
    val daysUnderGoal: Int = 0,
    val totalCalories: Int = 0
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _weekStart = MutableStateFlow(
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    )

    val uiState: StateFlow<AnalyticsUiState> = combine(
        _weekStart,
        settingsRepository.settings,
        mealRepository.getMealsBetweenDates(
            _weekStart.value,
            _weekStart.value.plusDays(6)
        )
    ) { weekStart, settings, allMeals ->
        val dayDataList = (0..6).map { dayOffset ->
            val date = weekStart.plusDays(dayOffset.toLong())
            val dayMeals = allMeals.filter { it.date == date }
            val calories = dayMeals.sumOf { it.calories }
            DayData(
                date = date,
                calories = calories,
                goal = settings.dailyCalorieGoal
            )
        }

        val avgCalories = if (dayDataList.isNotEmpty()) {
            dayDataList.sumOf { it.calories } / dayDataList.size
        } else 0

        val daysOver = dayDataList.count { it.calories > it.goal }
        val daysUnder = dayDataList.count { it.calories > 0 && it.calories <= it.goal }

        AnalyticsUiState(
            weekStart = weekStart,
            weekData = dayDataList,
            avgCalories = avgCalories,
            daysOverGoal = daysOver,
            daysUnderGoal = daysUnder,
            totalCalories = dayDataList.sumOf { it.calories }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())

    fun previousWeek() {
        _weekStart.value = _weekStart.value.minusWeeks(1)
        refreshData()
    }

    fun nextWeek() {
        _weekStart.value = _weekStart.value.plusWeeks(1)
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch {
            val weekStart = _weekStart.value
            mealRepository.getMealsBetweenDates(weekStart, weekStart.plusDays(6)).first()
        }
    }
}
