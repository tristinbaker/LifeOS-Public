package com.lifeos.modules.lifeos_mealtracker.domain.usecase

import com.lifeos.modules.lifeos_mealtracker.data.repository.MealRepository
import com.lifeos.modules.lifeos_mealtracker.data.repository.SettingsRepository
import com.lifeos.modules.lifeos_mealtracker.data.repository.UserSettings
import com.lifeos.modules.lifeos_mealtracker.domain.model.DailyTotals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class CalculateDailyTotalsUseCase @Inject constructor(
    private val mealRepository: MealRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(date: LocalDate): Flow<Pair<DailyTotals, UserSettings>> {
        return combine(
            mealRepository.getDailyTotals(date),
            settingsRepository.settings
        ) { totals, settings ->
            totals to settings
        }
    }
}
