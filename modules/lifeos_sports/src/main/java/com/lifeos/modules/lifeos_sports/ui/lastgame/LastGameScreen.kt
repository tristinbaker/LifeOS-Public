package com.lifeos.modules.lifeos_sports.ui.lastgame

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.lifeos.modules.lifeos_sports.domain.model.FavoriteTeam
import com.lifeos.modules.lifeos_sports.domain.model.GameDetails
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LastGameScreen(
    initialTeamId: String? = null,
    onScrolled: () -> Unit = {},
    viewModel: LastGameViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val teamsWithGames = if (state.isLoading) state.favorites
        else state.favorites.filter { state.gameDetails[it.id] != null }
    val pagerState = rememberPagerState(pageCount = { maxOf(1, teamsWithGames.size) })

    LaunchedEffect(initialTeamId, teamsWithGames) {
        if (initialTeamId != null && teamsWithGames.isNotEmpty()) {
            val idx = teamsWithGames.indexOfFirst { it.id == initialTeamId }
            if (idx >= 0) pagerState.animateScrollToPage(idx)
            onScrolled()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Last Game") },
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
            state.favorites.isEmpty() || (!state.isLoading && teamsWithGames.isEmpty()) -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text("No teams selected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("Go to My Teams tab to pick your favorites", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (teamsWithGames.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(teamsWithGames.size) { index ->
                                val selected = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(if (selected) 10.dp else 7.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.fillMaxSize()
                                    ) {}
                                }
                            }
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val team = teamsWithGames[page]
                        val details = state.gameDetails[team.id]
                        GameDetailsPage(team = team, details = details)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameDetailsPage(team: FavoriteTeam, details: GameDetails?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = team.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = team.league.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        if (details == null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No recent completed game found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return
        }

        // Score card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = formatDate(details.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamResultColumn(
                        abbreviation = details.awayTeam.abbreviation,
                        score = details.awayTeam.score ?: "-",
                        isWinner = (details.awayTeam.score?.toIntOrNull() ?: 0) > (details.homeTeam.score?.toIntOrNull() ?: 0)
                    )
                    Text("@", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TeamResultColumn(
                        abbreviation = details.homeTeam.abbreviation,
                        score = details.homeTeam.score ?: "-",
                        isWinner = (details.homeTeam.score?.toIntOrNull() ?: 0) > (details.awayTeam.score?.toIntOrNull() ?: 0)
                    )
                }
                if (details.venue.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = details.venue,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (details.attendance.isNotBlank()) {
                    Text(
                        text = "Attendance: ${details.attendance}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Headlines
        if (details.headlines.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Recap", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    details.headlines.forEach { headline ->
                        Text(text = headline, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Stat leaders
        if (details.leaders.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Key Performers", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    details.leaders.forEach { leader ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(leader.athleteName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Text(
                                    text = leader.teamAbbreviation,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = leader.displayValue,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamResultColumn(abbreviation: String, score: String, isWinner: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = abbreviation,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = score,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = if (isWinner) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFmt = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
        outputFmt.format(inputFmt.parse(dateStr) ?: return dateStr)
    } catch (e: Exception) {
        dateStr
    }
}
