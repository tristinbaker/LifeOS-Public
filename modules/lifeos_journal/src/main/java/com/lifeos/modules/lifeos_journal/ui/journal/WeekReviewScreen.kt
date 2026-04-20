package com.lifeos.modules.lifeos_journal.ui.journal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lifeos.modules.lifeos_journal.data.local.JournalImageEntity
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekReviewScreen(
    images: List<JournalImageEntity>,
    weekLabel: String,
    onClose: () -> Unit
) {
    if (images.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(32.dp)
            ) {
                Text(
                    text = weekLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "No photos this week",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onClose) {
                    Text("Close", color = Color.White)
                }
            }
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = 0) { images.size }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = File(images[page].localPath),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = weekLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Text(
            text = "${pagerState.currentPage + 1} / ${images.size}",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 28.dp),
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelMedium
        )

        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dotCount = minOf(images.size, 20)
                repeat(dotCount) { i ->
                    val isSelected = i == pagerState.currentPage
                    Box(
                        Modifier
                            .size(if (isSelected) 8.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }
    }
}
