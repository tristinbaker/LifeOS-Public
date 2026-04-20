package com.lifeos.modules.lifeos_sports.ui.scores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.modules.lifeos_sports.ui.components.ScoreCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoresScreen(
    onGameClick: (teamId: String) -> Unit = {},
    viewModel: ScoresViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Today's Games") },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.favoriteTeamIds.isEmpty() -> EmptyFavoritesMessage()
            state.scoresByLeague.isEmpty() && !state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No live or upcoming games today", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.refresh() }) { Text("Refresh") }
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.scoresByLeague.forEach { (league, games) ->
                        item {
                            Text(
                                text = league.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(games, key = { it.id }) { game ->
                            val favoriteTeamId = when {
                                game.homeTeam.id in state.favoriteTeamIds -> game.homeTeam.id
                                game.awayTeam.id in state.favoriteTeamIds -> game.awayTeam.id
                                else -> null
                            }
                            ScoreCard(
                                game = game,
                                onClick = favoriteTeamId?.let { id -> { onGameClick(id) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(
                text = "No teams selected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Go to My Teams tab to pick your favorite teams",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
