package com.lifeos.modules.lifeos_sports.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_sports.data.repository.SportsRepository
import com.lifeos.modules.lifeos_sports.domain.model.FavoriteTeam
import com.lifeos.modules.lifeos_sports.domain.model.League
import com.lifeos.modules.lifeos_sports.notification.GameAlertScheduler
import com.lifeos.modules.lifeos_sports.notification.GameNotificationStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val selectedLeague: League = League.MLB,
    val availableTeams: List<FavoriteTeam> = emptyList(),
    val favoriteCompositeIds: Set<String> = emptySet(),
    val notificationCompositeIds: Set<String> = emptySet(),
    val isLoadingTeams: Boolean = false,
    val searchQuery: String = "",
    val pendingNotifTeam: FavoriteTeam? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SportsRepository,
    private val notificationStore: GameNotificationStore,
    private val scheduler: GameAlertScheduler
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
        viewModelScope.launch {
            notificationStore.notificationTeamIds.collect { ids ->
                _state.update { it.copy(notificationCompositeIds = ids) }
            }
        }
        scheduler.scheduleDailyCheck()
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
                // Also disable notifications when removing from favorites
                notificationStore.setEnabled("${team.league.name}_${team.id}", false)
            } else {
                repository.addFavorite(team)
            }
        }
    }

    fun toggleNotification(team: FavoriteTeam) {
        val compositeId = "${team.league.name}_${team.id}"
        val currentlyEnabled = compositeId in _state.value.notificationCompositeIds

        if (!currentlyEnabled) {
            val granted = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            if (!granted) {
                _state.update { it.copy(pendingNotifTeam = team) }
                return
            }
        }

        viewModelScope.launch {
            notificationStore.setEnabled(compositeId, !currentlyEnabled)
            if (!currentlyEnabled) scheduler.scheduleNow()
        }
    }

    fun onNotificationPermissionGranted() {
        val team = _state.value.pendingNotifTeam ?: return
        _state.update { it.copy(pendingNotifTeam = null) }
        val compositeId = "${team.league.name}_${team.id}"
        viewModelScope.launch {
            notificationStore.setEnabled(compositeId, true)
            scheduler.scheduleNow()
        }
    }

    fun onNotificationPermissionDenied() {
        _state.update { it.copy(pendingNotifTeam = null) }
    }

    private fun loadTeamsForLeague(league: League) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingTeams = true) }
            val teams = repository.getTeamsForLeague(league)
            _state.update { it.copy(availableTeams = teams, isLoadingTeams = false) }
        }
    }
}
