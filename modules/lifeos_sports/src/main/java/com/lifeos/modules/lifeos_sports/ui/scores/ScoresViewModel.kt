package com.lifeos.modules.lifeos_sports.ui.scores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_sports.data.repository.SportsRepository
import com.lifeos.modules.lifeos_sports.domain.model.FavoriteTeam
import com.lifeos.modules.lifeos_sports.domain.model.GameScore
import com.lifeos.modules.lifeos_sports.domain.model.GameStatus
import com.lifeos.modules.lifeos_sports.domain.model.League
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScoresUiState(
    val isLoading: Boolean = false,
    val scoresByLeague: Map<League, List<GameScore>> = emptyMap(),
    val favoriteTeamIds: Set<String> = emptySet(),
    val error: String? = null
)

@HiltViewModel
class ScoresViewModel @Inject constructor(
    private val repository: SportsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScoresUiState())
    val state: StateFlow<ScoresUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFavoriteTeams().collect { favorites ->
                _state.update { it.copy(favoriteTeamIds = favorites.map { t -> t.id }.toSet()) }
                if (favorites.isNotEmpty()) loadScores(favorites)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val favorites = repository.getFavoriteTeams().first()
            if (favorites.isNotEmpty()) loadScores(favorites)
        }
    }

    private suspend fun loadScores(favorites: List<FavoriteTeam>) {
        _state.update { it.copy(isLoading = true, error = null) }
        val leaguesToFetch = favorites.map { it.league }.distinct()
        val result = mutableMapOf<League, List<GameScore>>()

        leaguesToFetch.forEach { league ->
            val scores = repository.getScoresForLeague(league)
            val favoriteIds = favorites.filter { it.league == league }.map { it.id }.toSet()
            val filtered = scores.filter { game ->
                game.status != GameStatus.POST &&
                (game.homeTeam.id in favoriteIds || game.awayTeam.id in favoriteIds)
            }
            if (filtered.isNotEmpty()) result[league] = filtered
        }
        _state.update { it.copy(isLoading = false, scoresByLeague = result) }
    }
}
