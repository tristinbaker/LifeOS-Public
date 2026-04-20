package com.lifeos.modules.lifeos_sports.ui.standings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_sports.data.repository.SportsRepository
import com.lifeos.modules.lifeos_sports.domain.model.FavoriteTeam
import com.lifeos.modules.lifeos_sports.domain.model.League
import com.lifeos.modules.lifeos_sports.domain.model.StandingsGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StandingsUiState(
    val isLoading: Boolean = false,
    val selectedLeague: League = League.MLB,
    val groups: List<StandingsGroup> = emptyList(),
    val favoriteTeamIds: Set<String> = emptySet(),
    val error: String? = null
)

@HiltViewModel
class StandingsViewModel @Inject constructor(
    private val repository: SportsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StandingsUiState())
    val state: StateFlow<StandingsUiState> = _state.asStateFlow()

    private var allFavorites: List<FavoriteTeam> = emptyList()

    init {
        viewModelScope.launch {
            repository.getFavoriteTeams().collect { favorites ->
                allFavorites = favorites
                _state.update { it.copy(
                    favoriteTeamIds = favorites
                        .filter { f -> f.league == it.selectedLeague }
                        .map { f -> f.id }
                        .toSet()
                )}
            }
        }
        loadStandings(League.MLB)
    }

    fun selectLeague(league: League) {
        _state.update { it.copy(
            selectedLeague = league,
            favoriteTeamIds = allFavorites.filter { it.league == league }.map { it.id }.toSet()
        )}
        loadStandings(league)
    }

    fun refresh() {
        loadStandings(_state.value.selectedLeague, forceRefresh = true)
    }

    private fun loadStandings(league: League, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val groups = repository.getStandingsForLeague(league, forceRefresh)
            _state.update { it.copy(isLoading = false, groups = groups) }
        }
    }
}
