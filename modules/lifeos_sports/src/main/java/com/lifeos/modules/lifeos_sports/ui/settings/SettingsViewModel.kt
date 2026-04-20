package com.lifeos.modules.lifeos_sports.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_sports.data.repository.SportsRepository
import com.lifeos.modules.lifeos_sports.domain.model.FavoriteTeam
import com.lifeos.modules.lifeos_sports.domain.model.League
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val selectedLeague: League = League.MLB,
    val availableTeams: List<FavoriteTeam> = emptyList(),
    // Composite keys ("MLB_2", "NHL_2") so cross-league ID collisions don't affect highlighting
    val favoriteCompositeIds: Set<String> = emptySet(),
    val isLoadingTeams: Boolean = false,
    val searchQuery: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SportsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFavoriteTeams().collect { favorites ->
                _state.update {
                    it.copy(favoriteCompositeIds = favorites.map { t -> "${t.league.name}_${t.id}" }.toSet())
                }
            }
        }
        loadTeamsForLeague(League.MLB)
    }

    fun selectLeague(league: League) {
        _state.update { it.copy(selectedLeague = league, searchQuery = "") }
        loadTeamsForLeague(league)
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun toggleFavorite(team: FavoriteTeam) {
        viewModelScope.launch {
            if (repository.isFavorite(team)) {
                repository.removeFavorite(team)
            } else {
                repository.addFavorite(team)
            }
        }
    }

    private fun loadTeamsForLeague(league: League) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingTeams = true) }
            val teams = repository.getTeamsForLeague(league)
            _state.update { it.copy(availableTeams = teams, isLoadingTeams = false) }
        }
    }
}
