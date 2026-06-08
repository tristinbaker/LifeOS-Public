package com.lifeos.modules.lifeos_aiinsights.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifeos.core.InsightProvider
import com.lifeos.modules.lifeos_aiinsights.data.repository.ReportState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiInsightsScreen(
    viewModel: AiInsightsViewModel,
    onNavigateBack: () -> Unit
) {
    val insightStates by viewModel.insightStates.collectAsState()
    val providers = viewModel.insightProviders

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Insights") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (providers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No insights available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(providers, key = { it.insightId }) { provider ->
                    val state = insightStates[provider.insightId] ?: ReportState.Idle
                    InsightCard(
                        provider = provider,
                        state = state,
                        onGenerate = { viewModel.generateInsight(provider.insightId) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun InsightCard(
    provider: InsightProvider,
    state: ReportState,
    onGenerate: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = provider.cardTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = provider.cardQuestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (state is ReportState.Loaded) {
                    IconButton(
                        onClick = onGenerate,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            when (state) {
                is ReportState.Idle -> {
                    Button(
                        onClick = onGenerate,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Generate")
                    }
                }
                is ReportState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Analyzing…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                is ReportState.Loaded -> {
                    HorizontalDivider()
                    Text(
                        text = state.content,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f
                    )
                    Text(
                        text = "Generated ${formatAge(state.generatedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                is ReportState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = onGenerate) { Text("Try Again") }
                }
            }
        }
    }
}

private fun formatAge(epochMillis: Long): String {
    val ageMs = System.currentTimeMillis() - epochMillis
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ageMs)
    val hours = TimeUnit.MILLISECONDS.toHours(ageMs)
    val days = TimeUnit.MILLISECONDS.toDays(ageMs)
    return when {
        minutes < 2 -> "just now"
        hours < 1 -> "$minutes minutes ago"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        days == 1L -> "yesterday"
        else -> {
            val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            date.format(DateTimeFormatter.ofPattern("MMM d"))
        }
    }
}
