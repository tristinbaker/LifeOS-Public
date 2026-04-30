package com.lifeos.modules.lifeos_sports.ui.standings

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.modules.lifeos_sports.domain.model.League
import com.lifeos.modules.lifeos_sports.domain.model.Standing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsScreen(viewModel: StandingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Standings") },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )

        // League selector
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

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.groups.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No standings available", style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.groups.forEach { group ->
                        item {
                            Text(
                                text = group.groupName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            )
                            StandingsTable(
                                standings = group.standings,
                                favoriteTeamIds = state.favoriteTeamIds
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StandingsTable(standings: List<Standing>, favoriteTeamIds: Set<String>) {
    val showL10 = standings.any { it.lastTen.isNotBlank() }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            StandingsHeader(showL10 = showL10)
            HorizontalDivider()
            standings.forEachIndexed { index, standing ->
                val isFavorite = standing.teamId in favoriteTeamIds
                StandingsRow(standing = standing, isFavorite = isFavorite, showL10 = showL10)
                if (index < standings.lastIndex) HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun StandingsHeader(showL10: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Team",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text("W", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(32.dp), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("L", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(32.dp), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("PCT", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(44.dp), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("GB", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(36.dp), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (showL10) {
            Text("L10", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(44.dp), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StandingsRow(standing: Standing, isFavorite: Boolean, showL10: Boolean) {
    val bg = if (isFavorite) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${standing.rank}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = standing.abbreviation,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isFavorite) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${standing.wins}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = "${standing.losses}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = standing.winPercent,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = standing.gamesBack,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
        if (showL10) {
            Text(
                text = standing.lastTen.ifBlank { "-" },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.End
            )
        }
    }
}
