package com.lifeos.modules.lifeos_mealtracker.ui.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_mealtracker.data.repository.SettingsRepository
import com.lifeos.modules.lifeos_mealtracker.data.repository.UserSettings
import com.lifeos.modules.lifeos_mealtracker.data.repository.WeightRepository
import com.lifeos.modules.lifeos_mealtracker.domain.model.WeightEntry
import com.lifeos.modules.lifeos_mealtracker.domain.usecase.EstimateGoalDateUseCase
import com.lifeos.modules.lifeos_mealtracker.domain.usecase.GoalEstimation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class WeightUiState(
    val weightEntries: List<WeightEntry> = emptyList(),
    val currentWeight: Double? = null,
    val settings: UserSettings = UserSettings(),
    val goalEstimation: GoalEstimation? = null
)

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val weightRepository: WeightRepository,
    private val settingsRepository: SettingsRepository,
    private val estimateGoalDateUseCase: EstimateGoalDateUseCase
) : ViewModel() {

    val uiState: StateFlow<WeightUiState> = combine(
        weightRepository.getAllWeightEntries(),
        settingsRepository.settings,
        settingsRepository.settings.flatMapLatest { settings ->
            estimateGoalDateUseCase(settings.goalWeight, settings.weeklyWeightGoalRate)
        }
    ) { entries, settings, estimation ->
        WeightUiState(
            weightEntries = entries.sortedBy { it.date },
            currentWeight = entries.maxByOrNull { it.date }?.weight,
            settings = settings,
            goalEstimation = estimation
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightUiState())

    fun addWeight(weight: Double, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            weightRepository.insertWeightEntry(
                WeightEntry(date = date, weight = weight)
            )
        }
    }

    fun deleteWeight(entry: WeightEntry) {
        viewModelScope.launch {
            weightRepository.deleteWeightEntry(entry)
        }
    }
}
