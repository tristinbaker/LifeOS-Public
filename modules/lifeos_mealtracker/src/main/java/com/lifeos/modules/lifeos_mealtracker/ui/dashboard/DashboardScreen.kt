package com.lifeos.modules.lifeos_mealtracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeos.modules.lifeos_mealtracker.ui.components.CalorieProgressRing
import com.lifeos.modules.lifeos_mealtracker.ui.components.MacroRow
import com.lifeos.modules.lifeos_mealtracker.ui.components.MealEntryCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddMeal: () -> Unit,
    onEditMeal: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = LocalDate.now()
    val isToday = uiState.selectedDate == today

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.navigateToPreviousDay() }
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
                        }
                        Text(
                            text = when {
                                isToday -> "Today"
                                uiState.selectedDate == today.minusDays(1) -> "Yesterday"
                                uiState.selectedDate == today.plusDays(1) -> "Tomorrow"
                                else -> uiState.selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { viewModel.navigateToNextDay() }
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMeal,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add meal")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CalorieProgressRing(
                        consumed = uiState.dailyTotals?.totalCalories ?: 0,
                        goal = uiState.settings.dailyCalorieGoal
                    )
                }
            }

            item {
                MacroRow(
                    protein = uiState.dailyTotals?.totalProtein ?: 0,
                    carbs = uiState.dailyTotals?.totalCarbs ?: 0,
                    fat = uiState.dailyTotals?.totalFat ?: 0,
                    proteinTarget = uiState.settings.targetProtein,
                    carbsTarget = uiState.settings.targetCarbs,
                    fatTarget = uiState.settings.targetFat
                )
            }

            item {
                Text(
                    text = "Today's Meals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            val meals = uiState.dailyTotals?.meals ?: emptyList()
            if (meals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No meals logged yet. Tap + to add one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(meals, key = { it.id }) { meal ->
                    MealEntryCard(
                        meal = meal,
                        onEdit = { onEditMeal(meal.id) },
                        onDelete = { viewModel.deleteMeal(meal) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
