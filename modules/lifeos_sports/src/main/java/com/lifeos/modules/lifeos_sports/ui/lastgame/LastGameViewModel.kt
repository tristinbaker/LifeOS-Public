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
                val cached = favorites.associate { team ->
                    compositeKey(team) to repository.getCachedLastGame(team.league, team.id)
                }
                _state.update { it.copy(gameDetails = cached) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val favorites = _state.value.favorites
            if (favorites.isEmpty()) return@launch
            _state.update { it.copy(isLoading = true, error = null) }
            val details = mutableMapOf<String, GameDetails?>()
            favorites.forEach { team ->
                details[compositeKey(team)] = repository.getLastCompletedGame(team.league, team.id, forceRefresh = true)
            }
            _state.update { it.copy(isLoading = false, gameDetails = details) }
        }
    }

    private fun compositeKey(team: FavoriteTeam) = "${team.league.name}_${team.id}"
}
