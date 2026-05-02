package com.lifeos.modules.lifeos_physicalmedia.ui.random

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lifeos.modules.lifeos_physicalmedia.domain.model.*
import com.lifeos.modules.lifeos_physicalmedia.ui.PhysicalMediaState

@Composable
fun RandomPickerScreen(
    type: String,
    state: PhysicalMediaState,
    onPickAnother: () -> Unit,
    onDone: () -> Unit
) {
    BackHandler(onBack = onDone)

    val title: String?
    val subtitle: String?
    val coverModel: Any?

    when (type) {
        "book" -> {
            val book = state.randomBook
            title = book?.title
            subtitle = book?.author?.takeIf { it.isNotBlank() }
            coverModel = book?.coverLocalPath?.takeIf { it.isNotBlank() } ?: book?.coverUrl?.takeIf { it.isNotBlank() }
        }
        "movie" -> {
            val movie = state.randomMovie
            val itemTitle = state.randomMovieItemTitle
            title = itemTitle ?: movie?.title
            subtitle = if (itemTitle != null) "from ${movie?.title}" else movie?.format?.displayName()
            coverModel = movie?.coverLocalPath?.takeIf { it.isNotBlank() } ?: movie?.coverUrl?.takeIf { it.isNotBlank() }
        }
        "game" -> {
            val game = state.randomGame
            val itemTitle = state.randomGameItemTitle
            title = itemTitle ?: game?.title
            subtitle = if (itemTitle != null) "from ${game?.title}" else game?.system?.displayName()
            coverModel = game?.coverLocalPath?.takeIf { it.isNotBlank() } ?: game?.coverUrl?.takeIf { it.isNotBlank() }
        }
        else -> { title = null; subtitle = null; coverModel = null }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = coverModel,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "cover-anim",
            modifier = Modifier.fillMaxWidth().weight(0.55f)
        ) { model ->
            if (model != null) {
                AsyncImage(
                    model = model,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        fontSize = 96.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (title == null) {
                Spacer(Modifier.weight(1f))
                Text(
                    "Nothing in your collection yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text("Done")
                }
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                if (state.randomExcludedCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Skipping ${state.randomExcludedCount} recently logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onPickAnother, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Casino, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Pick Another")
                    }
                    Button(onClick = onDone, modifier = Modifier.weight(1f)) {
                        Text("That'll Do!")
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
