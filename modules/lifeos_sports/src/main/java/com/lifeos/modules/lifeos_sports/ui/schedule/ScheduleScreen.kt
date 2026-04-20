package com.lifeos.modules.lifeos_sports.ui.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.modules.lifeos_sports.domain.model.GameScore
import com.lifeos.modules.lifeos_sports.domain.model.League
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Schedule") },
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
            state.schedulesByLeague.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text("No upcoming games", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("Add teams in My Teams to see their schedule", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.schedulesByLeague.forEach { (league, teamSchedules) ->
                        item(key = "header_${league.name}") {
                            Text(
                                text = league.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        teamSchedules.forEach { teamSchedule ->
                            item(key = "${teamSchedule.team.league.name}_${teamSchedule.team.id}") {
                                TeamScheduleCard(teamSchedule)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamScheduleCard(teamSchedule: TeamSchedule) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = teamSchedule.team.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            if (teamSchedule.upcomingGames.isEmpty()) {
                Text(
                    text = "No upcoming games found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                teamSchedule.upcomingGames.forEach { game ->
                    UpcomingGameRow(game = game, myTeamId = teamSchedule.team.id)
                }
            }
        }
    }
}

@Composable
private fun UpcomingGameRow(game: GameScore, myTeamId: String) {
    val isHome = game.homeTeam.id == myTeamId
    val opponent = if (isHome) game.awayTeam else game.homeTeam
    val homeAway = if (isHome) "vs" else "@"

    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$homeAway ${opponent.abbreviation}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = formatGameDateTime(game.date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
    }
}

private fun formatGameDateTime(dateStr: String): String {
    return try {
        val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val parsed = inputFmt.parse(dateStr) ?: return dateStr
        val outputFmt = SimpleDateFormat("EEE MMM d · h:mm a", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
        outputFmt.format(parsed)
    } catch (e: Exception) {
        dateStr
    }
}
