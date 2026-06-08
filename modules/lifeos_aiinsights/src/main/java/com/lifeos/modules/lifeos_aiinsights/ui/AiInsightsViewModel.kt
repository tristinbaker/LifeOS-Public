package com.lifeos.modules.lifeos_aiinsights.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.core.InsightProvider
import com.lifeos.modules.lifeos_aiinsights.data.repository.AiInsightsRepository
import com.lifeos.modules.lifeos_aiinsights.data.repository.ReportState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiInsightsViewModel @Inject constructor(
    private val repository: AiInsightsRepository
) : ViewModel() {

    val insightProviders: List<InsightProvider> = repository.getOrderedInsightProviders()

    private val _insightStates = MutableStateFlow<Map<String, ReportState>>(
        insightProviders.associate { it.insightId to ReportState.Idle }
    )
    val insightStates: StateFlow<Map<String, ReportState>> = _insightStates.asStateFlow()

    init {
        viewModelScope.launch {
            insightProviders.forEach { provider ->
                val cached = repository.loadCachedInsight(provider.insightId)
                if (cached is ReportState.Loaded) {
                    _insightStates.update { it + (provider.insightId to cached) }
                }
            }
        }
    }

    fun generateInsight(insightId: String) {
        if (_insightStates.value[insightId] is ReportState.Loading) return
        viewModelScope.launch {
            _insightStates.update { it + (insightId to ReportState.Loading) }
            val result = repository.generateInsight(insightId)
            _insightStates.update { it + (insightId to result) }
        }
    }
}
