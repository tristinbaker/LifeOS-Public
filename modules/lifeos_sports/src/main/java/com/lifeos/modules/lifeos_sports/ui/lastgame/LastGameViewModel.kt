package com.lifeos.modules.lifeos_sports.ui.lastgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_sports.data.repository.SportsRepository
import com.lifeos.modules.lifeos_sports.domain.model.FavoriteTeam
import com.lifeos.modules.lifeos_sports.domain.model.GameDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LastGameUiState(
    val isLoading: Boolean = false,
    val favorites: List<FavoriteTeam> = emptyList(),
    val gameDetails: Map<String, GameDetails?> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class LastGameViewModel @Inject constructor(
    private val repository: SportsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LastGameUiState())
    val state: StateFlow<LastGameUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFavoriteTeams().collect { favorites ->
                _state.update { it.copy(favorites = favorites) }
                if (favorites.isNotEmpty()) loadLastGames(favorites)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val favorites = repository.getFavoriteTeams().first()
            if (favorites.isNotEmpty()) loadLastGames(favorites, forceRefresh = true)
        }
    }

    private suspend fun loadLastGames(favorites: List<FavoriteTeam>, forceRefresh: Boolean = false) {
        if (!forceRefresh) {
            val cached = mutableMapOf<String, GameDetails?>()
            favorites.forEach { team ->
                cached[compositeKey(team)] = repository.getCachedLastGame(team.league, team.id)
            }
            val allCached = favorites.all { cached[compositeKey(it)] != null }
            if (allCached) {
                _state.update { it.copy(gameDetails = cached) }
                return
            }
            if (cached.values.any { it != null }) {
                _state.update { it.copy(gameDetails = cached) }
            }
        }

        _state.update { it.copy(isLoading = true, error = null) }
        val details = _state.value.gameDetails.toMutableMap()
        favorites.forEach { team ->
            if (forceRefresh || details[compositeKey(team)] == null) {
                details[compositeKey(team)] = repository.getLastCompletedGame(team.league, team.id, forceRefresh)
            }
        }
        _state.update { it.copy(isLoading = false, gameDetails = details) }
    }

    private fun compositeKey(team: FavoriteTeam) = "${team.league.name}_${team.id}"
}
