package com.lifeos.modules.lifeos_mealtracker.domain.usecase

import com.lifeos.modules.lifeos_mealtracker.data.remote.FatSecretApiClient
import com.lifeos.modules.lifeos_mealtracker.data.repository.StoredItemRepository
import com.lifeos.modules.lifeos_mealtracker.domain.model.FatSecretServing
import com.lifeos.modules.lifeos_mealtracker.domain.model.StoredItem
import javax.inject.Inject

sealed class BarcodeLookupResult {
    data class CachedItem(val item: StoredItem) : BarcodeLookupResult()
    data class RemoteServings(val foodName: String, val servings: List<FatSecretServing>) : BarcodeLookupResult()
    data object NotFound : BarcodeLookupResult()
    data class Error(val message: String) : BarcodeLookupResult()
}

class LookupBarcodeUseCase @Inject constructor(
    private val storedItemRepository: StoredItemRepository,
    private val fatSecretApiClient: FatSecretApiClient
) {
    suspend operator fun invoke(barcode: String): BarcodeLookupResult {
        val cached = storedItemRepository.getStoredItemByBarcode(barcode)
        if (cached != null) return BarcodeLookupResult.CachedItem(cached)

        return try {
            val foodId = fatSecretApiClient.getFoodIdForBarcode(barcode)
                ?: return BarcodeLookupResult.NotFound
            val (foodName, servings) = fatSecretApiClient.getServingsForFood(foodId)
            if (servings.isEmpty()) return BarcodeLookupResult.NotFound
            BarcodeLookupResult.RemoteServings(foodName, servings)
        } catch (e: Exception) {
            BarcodeLookupResult.Error(e.message ?: "Network error")
        }
    }
}
