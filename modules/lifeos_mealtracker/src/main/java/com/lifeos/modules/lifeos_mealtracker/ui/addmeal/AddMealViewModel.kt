package com.lifeos.modules.lifeos_mealtracker.ui.addmeal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_mealtracker.data.repository.MealRepository
import com.lifeos.modules.lifeos_mealtracker.data.repository.SavedMealRepository
import com.lifeos.modules.lifeos_mealtracker.data.repository.SettingsRepository
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealEntry
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealType
import com.lifeos.modules.lifeos_mealtracker.domain.model.SavedMeal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddMealUiState(
    val isEditing: Boolean = false,
    val mealId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val mealType: MealType = MealType.BREAKFAST,
    val name: String = "",
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val saveAsFavorite: Boolean = false,
    val savedMeals: List<SavedMeal> = emptyList(),
    val dailyCalorieGoal: Int = 2000,
    val isLoading: Boolean = false,
    val shouldShowShame: Boolean = false
)

@HiltViewModel
class AddMealViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val savedMealRepository: SavedMealRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMealUiState())
    val uiState: StateFlow<AddMealUiState> = _uiState.asStateFlow()

    private var editingMeal: MealEntry? = null
    private var previousDayCalories: Int = 0

    init {
        viewModelScope.launch {
            combine(
                savedMealRepository.getAllSavedMeals(),
                settingsRepository.settings
            ) { meals, settings ->
                _uiState.update {
                    it.copy(
                        savedMeals = meals,
                        dailyCalorieGoal = settings.dailyCalorieGoal
                    )
                }
            }.collect()
        }
    }

    fun loadMeal(mealId: Long) {
        viewModelScope.launch {
            val meal = mealRepository.getMealById(mealId)
            if (meal != null) {
                editingMeal = meal
                _uiState.update {
                    it.copy(
                        isEditing = true,
                        mealId = meal.id,
                        date = meal.date,
                        mealType = meal.mealType,
                        name = meal.name ?: "",
                        calories = meal.calories.toString(),
                        protein = meal.protein.toString(),
                        carbs = meal.carbs.toString(),
                        fat = meal.fat.toString()
                    )
                }
            }
        }
    }

    fun loadSavedMeal(savedMealId: Long) {
        viewModelScope.launch {
            val savedMeal = savedMealRepository.getSavedMealById(savedMealId)
            if (savedMeal != null) {
                _uiState.update {
                    it.copy(
                        mealType = savedMeal.mealType,
                        name = savedMeal.name,
                        calories = savedMeal.calories.toString(),
                        protein = savedMeal.protein.toString(),
                        carbs = savedMeal.carbs.toString(),
                        fat = savedMeal.fat.toString()
                    )
                }
            }
        }
    }

    fun updateDate(date: LocalDate) {
        viewModelScope.launch {
            if (date != _uiState.value.date) {
                val previousTotals = mealRepository.getDailyTotals(date).first()
                previousDayCalories = previousTotals.totalCalories
            }
            _uiState.update { it.copy(date = date) }
        }
    }

    fun updateMealType(mealType: MealType) {
        _uiState.update { it.copy(mealType = mealType) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateCalories(calories: String) {
        _uiState.update { it.copy(calories = calories.filter { c -> c.isDigit() }) }
    }

    fun updateProtein(protein: String) {
        _uiState.update { it.copy(protein = protein.filter { c -> c.isDigit() }) }
    }

    fun updateCarbs(carbs: String) {
        _uiState.update { it.copy(carbs = carbs.filter { c -> c.isDigit() }) }
    }

    fun updateFat(fat: String) {
        _uiState.update { it.copy(fat = fat.filter { c -> c.isDigit() }) }
    }

    fun updateSaveAsFavorite(save: Boolean) {
        _uiState.update { it.copy(saveAsFavorite = save) }
    }

    fun saveMeal(onSuccess: () -> Unit, onShowShame: (Int) -> Unit) {
        val state = _uiState.value
        val calories = state.calories.toIntOrNull() ?: 0

        if (calories == 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val meal = MealEntry(
                id = state.mealId ?: 0,
                date = state.date,
                mealType = state.mealType,
                name = state.name.takeIf { it.isNotBlank() },
                calories = calories,
                protein = state.protein.toIntOrNull() ?: 0,
                carbs = state.carbs.toIntOrNull() ?: 0,
                fat = state.fat.toIntOrNull() ?: 0
            )

            if (state.isEditing) {
                mealRepository.updateMeal(meal)
            } else {
                mealRepository.insertMeal(meal)
            }

            if (state.saveAsFavorite && !state.isEditing) {
                savedMealRepository.insertSavedMeal(
                    SavedMeal(
                        name = state.name.takeIf { it.isNotBlank() } ?: state.mealType.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        mealType = state.mealType,
                        calories = calories,
                        protein = state.protein.toIntOrNull() ?: 0,
                        carbs = state.carbs.toIntOrNull() ?: 0,
                        fat = state.fat.toIntOrNull() ?: 0
                    )
                )
            }

            val totals = mealRepository.getDailyTotals(state.date).first()
            val newTotal = totals.totalCalories

            if (newTotal > state.dailyCalorieGoal && editingMeal == null) {
                val excess = newTotal - state.dailyCalorieGoal
                onShowShame(excess)
            }

            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }
}
