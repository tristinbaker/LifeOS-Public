package com.lifeos.modules.lifeos_aiinsights.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_aiinsights.data.repository.AiInsightsRepository
import com.lifeos.modules.lifeos_aiinsights.data.repository.ReportState
import com.lifeos.modules.lifeos_aiinsights.data.repository.ReportType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

enum class ReportTab { WEEKLY, MONTHLY }

data class AiInsightsUiState(
    val selectedTab: ReportTab = ReportTab.WEEKLY,
    val weeklyState: ReportState = ReportState.Idle,
    val monthlyState: ReportState = ReportState.Idle
)

@HiltViewModel
class AiInsightsViewModel @Inject constructor(
    private val repository: AiInsightsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiInsightsUiState())
    val uiState: StateFlow<AiInsightsUiState> = _uiState.asStateFlow()

    private val weekStart: LocalDate get() =
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    private val monthStart: LocalDate get() = YearMonth.now().atDay(1)

    init {
        viewModelScope.launch {
            val weekly = repository.loadCached(ReportType.WEEKLY, weekStart.toString())
            val monthly = repository.loadCached(ReportType.MONTHLY, monthStart.toString())
            _uiState.update { it.copy(weeklyState = weekly, monthlyState = monthly) }
        }
    }

    fun selectTab(tab: ReportTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun generateWeekly() {
        viewModelScope.launch {
            _uiState.update { it.copy(weeklyState = ReportState.Loading) }
            val result = repository.generateReport(ReportType.WEEKLY, weekStart)
            _uiState.update { it.copy(weeklyState = result) }
        }
    }

    fun generateMonthly() {
        viewModelScope.launch {
            _uiState.update { it.copy(monthlyState = ReportState.Loading) }
            val result = repository.generateReport(ReportType.MONTHLY, monthStart)
            _uiState.update { it.copy(monthlyState = result) }
        }
    }
}
