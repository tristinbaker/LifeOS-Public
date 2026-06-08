package com.lifeos.modules.lifeos_habittracker.ui.habits

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifeos.modules.lifeos_habittracker.data.local.HabitFrequency
import com.lifeos.modules.lifeos_habittracker.ui.HabitWithStats
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsListScreen(
    habitsWithStats: List<HabitWithStats>,
    onToggleCheckIn: (Long) -> Unit,
    onToggleCheckInForDate: (Long, LocalDate) -> Unit,
    onHabitClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Habits") },
            actions = {
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, "Add habit")
                }
            }
        )

        if (habitsWithStats.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No habits yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap + to add your first habit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(habitsWithStats, key = { it.habit.id }) { habitStats ->
                    HabitCard(
                        habitStats = habitStats,
                        onToggle = { onToggleCheckIn(habitStats.habit.id) },
                        onToggleDate = { date -> onToggleCheckInForDate(habitStats.habit.id, date) },
                        onClick = { onHabitClick(habitStats.habit.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HabitCard(
    habitStats: HabitWithStats,
    onToggle: () -> Unit,
    onToggleDate: (LocalDate) -> Unit,
    onClick: () -> Unit
) {
    var showBacklog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { showBacklog = true }
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (habitStats.isCheckedInToday)
                        Icons.Filled.CheckCircle
                    else
                        Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (habitStats.isCheckedInToday) "Checked in" else "Not checked in",
                    tint = if (habitStats.isCheckedInToday)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habitStats.habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                if (habitStats.habit.description.isNotEmpty()) {
                    Text(
                        text = habitStats.habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = getFrequencyText(habitStats.habit.frequency, habitStats.habit.daysOfWeek, habitStats.habit.timesPerWeek),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (habitStats.streak > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${habitStats.streak}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (habitStats.streak > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Timeline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${habitStats.totalCheckIns}/${habitStats.daysSinceStart}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    if (showBacklog) {
        BacklogBottomSheet(
            habitStats = habitStats,
            onDismiss = { showBacklog = false },
            onToggleDate = onToggleDate
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BacklogBottomSheet(
    habitStats: HabitWithStats,
    onDismiss: () -> Unit,
    onToggleDate: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = habitStats.habit.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Long-press any day to log it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                days.forEach { date ->
                    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val isChecked = dateStr in habitStats.recentCheckInDates
                    DayCell(
                        date = date,
                        isChecked = isChecked,
                        isToday = date == today,
                        onClick = { onToggleDate(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isChecked: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2)
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) primary else muted
        )
        Text(
            text = "${date.dayOfMonth}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) primary else onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Icon(
            imageVector = if (isChecked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (isChecked) "Checked" else "Not checked",
            tint = if (isChecked) primary else muted.copy(alpha = 0.4f),
            modifier = Modifier.size(28.dp)
        )
    }
}

private fun getFrequencyText(frequency: HabitFrequency, daysOfWeek: String, timesPerWeek: Int = 7): String {
    return when (frequency) {
        HabitFrequency.DAILY -> "Daily"
        HabitFrequency.SPECIFIC_DAYS -> {
            val dayNames = mapOf(
                "1" to "Mon", "2" to "Tue", "3" to "Wed",
                "4" to "Thu", "5" to "Fri", "6" to "Sat", "7" to "Sun"
            )
            daysOfWeek.split(",")
                .mapNotNull { dayNames[it.trim()] }
                .joinToString(", ")
        }
        HabitFrequency.TIMES_PER_WEEK -> "$timesPerWeek ${if (timesPerWeek == 1) "Time" else "Times"} Per Week"
    }
}
