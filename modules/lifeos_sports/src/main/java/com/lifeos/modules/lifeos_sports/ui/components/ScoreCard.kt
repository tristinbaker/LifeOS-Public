package com.lifeos.modules.lifeos_sports.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_sports.domain.model.GameScore
import com.lifeos.modules.lifeos_sports.domain.model.GameSituation
import com.lifeos.modules.lifeos_sports.domain.model.GameStatus
import com.lifeos.modules.lifeos_sports.domain.model.League
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScoreCard(game: GameScore, onClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        when (game.status) {
            GameStatus.PRE -> UpcomingContent(game)
            GameStatus.IN -> LiveContent(game)
            GameStatus.POST -> {} // filtered before reaching here
        }
    }
}

@Composable
private fun UpcomingContent(game: GameScore) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Away team
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = game.awayTeam.abbreviation,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (!game.awayTeam.record.isNullOrBlank()) {
                Text(
                    text = game.awayTeam.record,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!game.awayTeam.probablePitcher.isNullOrBlank()) {
                Text(
                    text = game.awayTeam.probablePitcher,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Middle: at + time
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1.4f)
        ) {
            Text(
                text = "@",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatTime(game.date),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = formatDateShort(game.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Home team
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = game.homeTeam.abbreviation,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (!game.homeTeam.record.isNullOrBlank()) {
                Text(
                    text = game.homeTeam.record,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!game.homeTeam.probablePitcher.isNullOrBlank()) {
                Text(
                    text = game.homeTeam.probablePitcher,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LiveContent(game: GameScore) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Status chip
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = game.period.ifBlank { "Live" },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        // Score row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiveTeamScore(game.awayTeam.abbreviation, game.awayTeam.score)
            Text(
                text = "–",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LiveTeamScore(game.homeTeam.abbreviation, game.homeTeam.score)
        }

        // Situation row
        game.situation?.let { situation ->
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            when (game.league) {
                League.MLB -> BaseballSituation(situation)
                League.NFL, League.NCAA_FOOTBALL -> FootballSituation(situation)
                else -> {}
            }
        }
    }
}

@Composable
private fun LiveTeamScore(abbreviation: String, score: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = score ?: "0",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = abbreviation,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BaseballSituation(situation: GameSituation) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bases diamond
        BasesDiamond(
            onFirst = situation.onFirst,
            onSecond = situation.onSecond,
            onThird = situation.onThird
        )

        // Count and outs
        Column(horizontalAlignment = Alignment.End) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("B", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                CountDots(filled = situation.balls ?: 0, total = 4, color = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(6.dp))
                Text("S", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                CountDots(filled = situation.strikes ?: 0, total = 3, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("O", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                CountDots(filled = situation.outs ?: 0, total = 3, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun FootballSituation(situation: GameSituation) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!situation.possession.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "🏈 ${situation.possession}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        if (!situation.downDistanceText.isNullOrBlank()) {
            Text(
                text = situation.downDistanceText,
                style = MaterialTheme.typography.bodySmall,
                color = if (situation.isRedZone) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BasesDiamond(onFirst: Boolean, onSecond: Boolean, onThird: Boolean) {
    val occupied = MaterialTheme.colorScheme.tertiary
    val empty = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val baseSize = 11.dp

    Box(Modifier.size(44.dp)) {
        // Second base — top center
        Box(
            Modifier
                .size(baseSize)
                .align(Alignment.TopCenter)
                .rotate(45f)
                .background(if (onSecond) occupied else empty)
        )
        // Third base — middle left
        Box(
            Modifier
                .size(baseSize)
                .align(Alignment.CenterStart)
                .rotate(45f)
                .background(if (onThird) occupied else empty)
        )
        // First base — middle right
        Box(
            Modifier
                .size(baseSize)
                .align(Alignment.CenterEnd)
                .rotate(45f)
                .background(if (onFirst) occupied else empty)
        )
    }
}

@Composable
private fun CountDots(filled: Int, total: Int, color: androidx.compose.ui.graphics.Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(total) { i ->
            Box(
                Modifier
                    .size(7.dp)
                    .background(
                        if (i < filled) color else color.copy(alpha = 0.2f),
                        RoundedCornerShape(50)
                    )
            )
        }
    }
}

private fun formatTime(dateStr: String): String {
    return try {
        val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val parsed = inputFmt.parse(dateStr) ?: return dateStr
        SimpleDateFormat("h:mm a", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }.format(parsed)
    } catch (e: Exception) { dateStr }
}

private fun formatDateShort(dateStr: String): String {
    return try {
        val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val parsed = inputFmt.parse(dateStr) ?: return dateStr
        SimpleDateFormat("EEE, MMM d", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }.format(parsed)
    } catch (e: Exception) { dateStr }
}
