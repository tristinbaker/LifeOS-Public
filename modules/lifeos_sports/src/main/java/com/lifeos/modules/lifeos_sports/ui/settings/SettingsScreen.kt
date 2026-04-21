package com.lifeos.modules.lifeos_sports.ui.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.modules.lifeos_sports.domain.model.FavoriteTeam
import com.lifeos.modules.lifeos_sports.domain.model.League

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.onNotificationPermissionGranted()
        else viewModel.onNotificationPermissionDenied()
    }

    LaunchedEffect(state.pendingNotifTeam) {
        if (state.pendingNotifTeam != null) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("My Teams") })

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            League.entries.forEach { league ->
                FilterChip(
                    selected = state.selectedLeague == league,
                    onClick = { viewModel.selectLeague(league) },
                    label = { Text(league.displayName) }
                )
            }
        }

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search teams…") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when {
            state.isLoadingTeams -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                val filtered = state.availableTeams.filter { team ->
                    state.searchQuery.isBlank() ||
                    team.name.contains(state.searchQuery, ignoreCase = true) ||
                    team.abbreviation.contains(state.searchQuery, ignoreCase = true)
                }.sortedBy { it.name }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    items(filtered, key = { "${it.league.name}_${it.id}" }) { team ->
                        val compositeId = "${team.league.name}_${team.id}"
                        val isFavorite = compositeId in state.favoriteCompositeIds
                        val notifEnabled = compositeId in state.notificationCompositeIds
                        TeamRow(
                            team = team,
                            isFavorite = isFavorite,
                            notificationsEnabled = notifEnabled,
                            onToggleFavorite = { viewModel.toggleFavorite(team) },
                            onToggleNotification = { viewModel.toggleNotification(team) }
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamRow(
    team: FavoriteTeam,
    isFavorite: Boolean,
    notificationsEnabled: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleNotification: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = team.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isFavorite) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = team.abbreviation,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isFavorite) {
            IconButton(onClick = onToggleNotification) {
                Icon(
                    imageVector = if (notificationsEnabled) Icons.Filled.Notifications else Icons.Outlined.NotificationsNone,
                    contentDescription = if (notificationsEnabled) "Disable game notifications" else "Enable game notifications",
                    tint = if (notificationsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
