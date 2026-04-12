package com.lifeos.modules.lifeos_mealtracker.ui.savedmeals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_mealtracker.data.repository.SavedMealRepository
import com.lifeos.modules.lifeos_mealtracker.domain.model.SavedMeal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedMealsViewModel @Inject constructor(
    private val savedMealRepository: SavedMealRepository
) : ViewModel() {

    val savedMeals: StateFlow<List<SavedMeal>> = savedMealRepository.getAllSavedMeals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSavedMeal(meal: SavedMeal) {
        viewModelScope.launch {
            savedMealRepository.deleteSavedMeal(meal)
        }
    }
}
