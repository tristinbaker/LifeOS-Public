package com.lifeos.modules.lifeos_medialogger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lifeos.modules.lifeos_medialogger.R
import com.lifeos.modules.lifeos_medialogger.domain.model.MediaItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MediaItemCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = item.coverLocalPath?.let { File(it) } ?: item.coverUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(80.dp, 120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (item.isRewatch) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Rewatch/Replay",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.hasPlatinum) {
                        Image(
                            painter = painterResource(R.drawable.platinum),
                            contentDescription = "Platinum",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (item.has100Percent) {
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (item.type == com.lifeos.modules.lifeos_medialogger.data.local.MediaType.GAME && item.platform != null) {
                    Text(
                        text = item.platform,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.type == com.lifeos.modules.lifeos_medialogger.data.local.MediaType.BOOK && item.author != null) {
                    Text(
                        text = item.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item.rating?.let { rating ->
                    RatingStars(rating = rating, size = 16)
                }

                item.dateCompleted?.let { date ->
                    Text(
                        text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RatingStars(
    rating: Float,
    size: Int = 20,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            val threshold = index + 1
            val icon = when {
                rating >= threshold -> Icons.Filled.Star
                rating >= threshold - 0.5f -> Icons.Filled.StarHalf
                else -> Icons.Outlined.StarOutline
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size.dp),
                tint = if (rating >= threshold - 0.5f) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )
        }
    }
}

@Composable
fun RatingSelector(
    selectedRating: Float?,
    onRatingSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val starCount = 5
    var componentWidth by remember { mutableFloatStateOf(0f) }

    fun ratingFromX(x: Float): Float {
        if (componentWidth == 0f) return 0f
        val starWidth = componentWidth / starCount
        val idx = (x / starWidth).toInt().coerceIn(0, starCount - 1)
        return if (x - idx * starWidth < starWidth / 2f) idx + 0.5f else (idx + 1).toFloat()
    }

    Row(
        modifier = modifier
            .onSizeChanged { componentWidth = it.width.toFloat() }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    onRatingSelected(ratingFromX(down.position.x))
                    drag(down.id) { change ->
                        onRatingSelected(ratingFromX(change.position.x))
                    }
                }
            }
    ) {
        repeat(starCount) { index ->
            val threshold = index + 1
            val icon = when {
                (selectedRating ?: 0f) >= threshold -> Icons.Filled.Star
                (selectedRating ?: 0f) >= threshold - 0.5f -> Icons.Filled.StarHalf
                else -> Icons.Outlined.StarOutline
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(4.dp),
                tint = if ((selectedRating ?: 0f) >= threshold - 0.5f) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )
        }
    }
}
