package com.lifeos.modules.lifeos_mealtracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_mealtracker.data.repository.MealRepository
import com.lifeos.modules.lifeos_mealtracker.data.repository.SettingsRepository
import com.lifeos.modules.lifeos_mealtracker.data.repository.UserSettings
import com.lifeos.modules.lifeos_mealtracker.domain.model.DailyTotals
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val dailyTotals: DailyTotals? = null,
    val settings: UserSettings = UserSettings()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<DashboardUiState> = combine(
        _selectedDate,
        mealRepository.getDailyTotals(LocalDate.now()),
        settingsRepository.settings
    ) { date, totals, settings ->
        DashboardUiState(
            selectedDate = date,
            dailyTotals = totals,
            settings = settings
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun deleteMeal(meal: MealEntry) {
        viewModelScope.launch {
            mealRepository.deleteMeal(meal)
        }
    }

    fun navigateToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun navigateToNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun navigateToToday() {
        _selectedDate.value = LocalDate.now()
    }
}
