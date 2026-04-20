package com.lifeos.modules.lifeos_sports.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_sports.data.repository.SportsRepository
import com.lifeos.modules.lifeos_sports.domain.model.FavoriteTeam
import com.lifeos.modules.lifeos_sports.domain.model.GameScore
import com.lifeos.modules.lifeos_sports.domain.model.League
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamSchedule(
    val team: FavoriteTeam,
    val upcomingGames: List<GameScore>
)

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val schedulesByLeague: Map<League, List<TeamSchedule>> = emptyMap()
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: SportsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleUiState())
    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFavoriteTeams().collect { favorites ->
                if (favorites.isNotEmpty()) loadSchedule(favorites)
                else _state.update { it.copy(isLoading = false, schedulesByLeague = emptyMap()) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val favorites = repository.getFavoriteTeams().first()
            if (favorites.isNotEmpty()) loadSchedule(favorites, forceRefresh = true)
        }
    }

    private suspend fun loadSchedule(favorites: List<FavoriteTeam>, forceRefresh: Boolean = false) {
        _state.update { it.copy(isLoading = true) }
        val schedulesByLeague = LinkedHashMap<League, MutableList<TeamSchedule>>()
        favorites.forEach { team ->
            val maxGames = when (team.league) {
                League.NFL, League.NCAA_FOOTBALL -> 1
                else -> 3
            }
            val games = repository.getUpcomingGamesForTeam(team.league, team.id, maxGames, forceRefresh)
            schedulesByLeague.getOrPut(team.league) { mutableListOf() }
                .add(TeamSchedule(team, games))
        }
        _state.update { it.copy(isLoading = false, schedulesByLeague = schedulesByLeague) }
    }
}
