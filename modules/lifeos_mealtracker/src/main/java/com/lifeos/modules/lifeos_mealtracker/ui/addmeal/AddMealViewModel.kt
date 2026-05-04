package com.lifeos.modules.lifeos_mealtracker.ui.addmeal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_mealtracker.data.repository.MealRepository
import com.lifeos.modules.lifeos_mealtracker.data.repository.SavedMealRepository
import com.lifeos.modules.lifeos_mealtracker.data.repository.SettingsRepository
import com.lifeos.modules.lifeos_mealtracker.data.repository.StoredItemRepository
import com.lifeos.modules.lifeos_mealtracker.domain.model.FatSecretServing
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealEntry
import com.lifeos.modules.lifeos_mealtracker.domain.model.MealType
import com.lifeos.modules.lifeos_mealtracker.domain.model.SavedMeal
import com.lifeos.modules.lifeos_mealtracker.domain.model.StoredItem
import com.lifeos.modules.lifeos_mealtracker.domain.usecase.BarcodeLookupResult
import com.lifeos.modules.lifeos_mealtracker.domain.usecase.LookupBarcodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class BarcodeState {
    data object Idle : BarcodeState()
    data object Loading : BarcodeState()
    data class ServingSelection(
        val barcode: String,
        val foodName: String,
        val servings: List<FatSecretServing>
    ) : BarcodeState()
    data class Error(val message: String) : BarcodeState()
}

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
    val storedItems: List<StoredItem> = emptyList(),
    val storedItemId: Long? = null,
    val quantity: Double = 1.0,
    val dailyCalorieGoal: Int = 2000,
    val isLoading: Boolean = false,
    val shouldShowShame: Boolean = false,
    val barcodeState: BarcodeState = BarcodeState.Idle,
    val pendingBarcode: String? = null,
    val showSaveToStoredItems: Boolean = false,
    val scannedBaseCalories: Int? = null,
    val scannedBaseProtein: Int? = null,
    val scannedBaseCarbs: Int? = null,
    val scannedBaseFat: Int? = null
)

@HiltViewModel
class AddMealViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val savedMealRepository: SavedMealRepository,
    private val settingsRepository: SettingsRepository,
    private val storedItemRepository: StoredItemRepository,
    private val lookupBarcodeUseCase: LookupBarcodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMealUiState())
    val uiState: StateFlow<AddMealUiState> = _uiState.asStateFlow()

    private var editingMeal: MealEntry? = null
    private var previousDayCalories: Int = 0

    init {
        viewModelScope.launch {
            combine(
                savedMealRepository.getAllSavedMeals(),
                storedItemRepository.getAllStoredItems(),
                settingsRepository.settings
            ) { meals, items, settings ->
                _uiState.update {
                    it.copy(
                        savedMeals = meals,
                        storedItems = items,
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

    fun loadStoredItem(storedItemId: Long, quantity: Double = 1.0) {
        viewModelScope.launch {
            val item = storedItemRepository.getStoredItemById(storedItemId)
            if (item != null) {
                val qty = if (quantity <= 0) 1.0 else quantity
                _uiState.update {
                    it.copy(
                        storedItemId = item.id,
                        name = item.name,
                        calories = (item.caloriesPerUnit * qty).toInt().toString(),
                        protein = (item.proteinPerUnit * qty).toInt().toString(),
                        carbs = (item.carbsPerUnit * qty).toInt().toString(),
                        fat = (item.fatPerUnit * qty).toInt().toString(),
                        quantity = qty,
                        scannedBaseCalories = null,
                        scannedBaseProtein = null,
                        scannedBaseCarbs = null,
                        scannedBaseFat = null
                    )
                }
            }
        }
    }

    fun updateQuantity(quantity: Double) {
        val state = _uiState.value
        val storedItem = state.storedItems.find { it.id == state.storedItemId }
        if (storedItem != null && quantity > 0) {
            _uiState.update {
                it.copy(
                    quantity = quantity,
                    calories = (storedItem.caloriesPerUnit * quantity).toInt().toString(),
                    protein = (storedItem.proteinPerUnit * quantity).toInt().toString(),
                    carbs = (storedItem.carbsPerUnit * quantity).toInt().toString(),
                    fat = (storedItem.fatPerUnit * quantity).toInt().toString()
                )
            }
        } else if (state.scannedBaseCalories != null && quantity > 0) {
            _uiState.update {
                it.copy(
                    quantity = quantity,
                    calories = ((state.scannedBaseCalories) * quantity).toInt().toString(),
                    protein = ((state.scannedBaseProtein ?: 0) * quantity).toInt().toString(),
                    carbs = ((state.scannedBaseCarbs ?: 0) * quantity).toInt().toString(),
                    fat = ((state.scannedBaseFat ?: 0) * quantity).toInt().toString()
                )
            }
        } else {
            _uiState.update { it.copy(quantity = quantity) }
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

    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(barcodeState = BarcodeState.Loading) }
            when (val result = lookupBarcodeUseCase(barcode)) {
                is BarcodeLookupResult.CachedItem -> {
                    val item = result.item
                    _uiState.update {
                        it.copy(
                            barcodeState = BarcodeState.Idle,
                            storedItemId = item.id,
                            name = item.name,
                            calories = item.caloriesPerUnit.toString(),
                            protein = item.proteinPerUnit.toString(),
                            carbs = item.carbsPerUnit.toString(),
                            fat = item.fatPerUnit.toString(),
                            quantity = 1.0,
                            pendingBarcode = null,
                            showSaveToStoredItems = false,
                            scannedBaseCalories = null,
                            scannedBaseProtein = null,
                            scannedBaseCarbs = null,
                            scannedBaseFat = null
                        )
                    }
                }
                is BarcodeLookupResult.RemoteServings -> {
                    _uiState.update {
                        it.copy(
                            barcodeState = BarcodeState.ServingSelection(
                                barcode = barcode,
                                foodName = result.foodName,
                                servings = result.servings
                            ),
                            pendingBarcode = barcode
                        )
                    }
                }
                is BarcodeLookupResult.NotFound -> {
                    _uiState.update {
                        it.copy(barcodeState = BarcodeState.Error("No food found for this barcode"))
                    }
                }
                is BarcodeLookupResult.Error -> {
                    _uiState.update {
                        it.copy(barcodeState = BarcodeState.Error(result.message))
                    }
                }
            }
        }
    }

    fun selectServing(serving: FatSecretServing) {
        val barcodeState = _uiState.value.barcodeState
        if (barcodeState !is BarcodeState.ServingSelection) return
        _uiState.update {
            it.copy(
                barcodeState = BarcodeState.Idle,
                name = barcodeState.foodName,
                calories = serving.calories.toString(),
                protein = serving.protein.toString(),
                carbs = serving.carbs.toString(),
                fat = serving.fat.toString(),
                storedItemId = null,
                quantity = 1.0,
                showSaveToStoredItems = true,
                scannedBaseCalories = serving.calories,
                scannedBaseProtein = serving.protein,
                scannedBaseCarbs = serving.carbs,
                scannedBaseFat = serving.fat
            )
        }
    }

    fun dismissBarcodeError() {
        _uiState.update { it.copy(barcodeState = BarcodeState.Idle) }
    }

    fun saveScannedFoodToStoredItems() {
        val state = _uiState.value
        if (!state.showSaveToStoredItems || state.pendingBarcode == null) return
        viewModelScope.launch {
            val newItem = StoredItem(
                name = state.name,
                caloriesPerUnit = state.scannedBaseCalories ?: (state.calories.toIntOrNull() ?: 0),
                proteinPerUnit = state.scannedBaseProtein ?: (state.protein.toIntOrNull() ?: 0),
                carbsPerUnit = state.scannedBaseCarbs ?: (state.carbs.toIntOrNull() ?: 0),
                fatPerUnit = state.scannedBaseFat ?: (state.fat.toIntOrNull() ?: 0),
                barcode = state.pendingBarcode
            )
            storedItemRepository.insertStoredItem(newItem)
            _uiState.update {
                it.copy(showSaveToStoredItems = false, pendingBarcode = null)
            }
        }
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
