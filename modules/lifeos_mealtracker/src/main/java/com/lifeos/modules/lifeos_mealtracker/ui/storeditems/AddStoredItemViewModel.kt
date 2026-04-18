package com.lifeos.modules.lifeos_mealtracker.ui.storeditems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_mealtracker.data.repository.StoredItemRepository
import com.lifeos.modules.lifeos_mealtracker.domain.model.StoredItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddStoredItemUiState(
    val isEditing: Boolean = false,
    val itemId: Long? = null,
    val name: String = "",
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class AddStoredItemViewModel @Inject constructor(
    private val storedItemRepository: StoredItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddStoredItemUiState())
    val uiState: StateFlow<AddStoredItemUiState> = _uiState.asStateFlow()

    fun loadItem(itemId: Long) {
        viewModelScope.launch {
            val item = storedItemRepository.getStoredItemById(itemId)
            if (item != null) {
                _uiState.update {
                    it.copy(
                        isEditing = true,
                        itemId = item.id,
                        name = item.name,
                        calories = item.caloriesPerUnit.toString(),
                        protein = item.proteinPerUnit.toString(),
                        carbs = item.carbsPerUnit.toString(),
                        fat = item.fatPerUnit.toString()
                    )
                }
            }
        }
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

    fun saveItem(onSuccess: () -> Unit) {
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val item = StoredItem(
                id = state.itemId ?: 0,
                name = state.name,
                caloriesPerUnit = state.calories.toIntOrNull() ?: 0,
                proteinPerUnit = state.protein.toIntOrNull() ?: 0,
                carbsPerUnit = state.carbs.toIntOrNull() ?: 0,
                fatPerUnit = state.fat.toIntOrNull() ?: 0
            )

            if (state.isEditing) {
                storedItemRepository.updateStoredItem(item)
            } else {
                storedItemRepository.insertStoredItem(item)
            }

            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }

    fun deleteItem(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.itemId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val item = storedItemRepository.getStoredItemById(state.itemId)
            item?.let { storedItemRepository.deleteStoredItem(it) }
            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }
}
