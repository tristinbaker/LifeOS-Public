package com.lifeos.modules.lifeos_mealtracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lifeos.modules.lifeos_mealtracker.ui.addmeal.AddMealScreen
import com.lifeos.modules.lifeos_mealtracker.ui.analytics.AnalyticsScreen
import com.lifeos.modules.lifeos_mealtracker.ui.dashboard.DashboardScreen
import com.lifeos.modules.lifeos_mealtracker.ui.savedmeals.SavedMealsScreen
import com.lifeos.modules.lifeos_mealtracker.ui.settings.SettingsScreen
import com.lifeos.modules.lifeos_mealtracker.ui.settings.SettingsViewModel
import com.lifeos.modules.lifeos_mealtracker.ui.shame.MotivationOverlay
import com.lifeos.modules.lifeos_mealtracker.ui.shame.ShameOverlay
import com.lifeos.modules.lifeos_mealtracker.ui.storeditems.AddStoredItemScreen
import com.lifeos.modules.lifeos_mealtracker.ui.weight.WeightScreen

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Dashboard : Screen("dashboard", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Analytics : Screen("analytics", "Stats", Icons.Filled.Analytics, Icons.Outlined.Analytics)
    data object SavedMeals : Screen("saved_meals", "Meals", Icons.Filled.Restaurant, Icons.Outlined.Restaurant)
    data object Weight : Screen("weight", "Weight", Icons.Filled.Scale, Icons.Outlined.Scale)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Analytics,
    Screen.SavedMeals,
    Screen.Weight,
    Screen.Settings
)

@Composable
fun BrutalMealTrackerNavHost(
    onShowShame: (Int) -> Unit
) {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val shameState by settingsViewModel.shameState.collectAsState()
    val motivationState by settingsViewModel.motivationState.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onAddMeal = { navController.navigate("add_meal") },
                    onEditMeal = { mealId -> navController.navigate("edit_meal/$mealId") }
                )
            }
            composable("add_meal") {
                AddMealScreen(
                    mealId = null,
                    onNavigateBack = { navController.popBackStack() },
                    onShowShame = onShowShame
                )
            }
            composable(
                route = "edit_meal/{mealId}",
                arguments = listOf(navArgument("mealId") { type = NavType.LongType })
            ) { backStackEntry ->
                val mealId = backStackEntry.arguments?.getLong("mealId")
                AddMealScreen(
                    mealId = mealId,
                    onNavigateBack = { navController.popBackStack() },
                    onShowShame = onShowShame
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }
            composable(Screen.SavedMeals.route) {
                SavedMealsScreen(
                    onMealSelected = { savedMeal ->
                        navController.navigate("add_meal?savedMealId=${savedMeal.id}")
                    },
                    onStoredItemSelected = { storedItem, quantity ->
                        navController.navigate("add_meal?storedItemId=${storedItem.id}&storedItemQuantity=$quantity")
                    },
                    onAddStoredItem = {
                        navController.navigate("add_stored_item")
                    },
                    onEditStoredItem = { itemId ->
                        navController.navigate("edit_stored_item/$itemId")
                    }
                )
            }
            composable(
                route = "add_meal?savedMealId={savedMealId}&storedItemId={storedItemId}&storedItemQuantity={storedItemQuantity}",
                arguments = listOf(
                    navArgument("savedMealId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("storedItemId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("storedItemQuantity") {
                        type = NavType.FloatType
                        defaultValue = 1f
                    }
                )
            ) { backStackEntry ->
                val savedMealId = backStackEntry.arguments?.getLong("savedMealId")?.takeIf { it != -1L }
                val storedItemId = backStackEntry.arguments?.getLong("storedItemId")?.takeIf { it != -1L }
                val storedItemQuantity = backStackEntry.arguments?.getFloat("storedItemQuantity")?.toDouble()
                AddMealScreen(
                    mealId = null,
                    savedMealId = savedMealId,
                    storedItemId = storedItemId,
                    storedItemQuantity = storedItemQuantity,
                    onNavigateBack = { navController.popBackStack() },
                    onShowShame = onShowShame
                )
            }
            composable("add_stored_item") {
                AddStoredItemScreen(
                    itemId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "edit_stored_item/{itemId}",
                arguments = listOf(
                    navArgument("itemId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getLong("itemId")
                AddStoredItemScreen(
                    itemId = itemId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Weight.route) {
                WeightScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }

    motivationState?.let { state ->
        MotivationOverlay(
            message = state.message,
            streak = state.streak,
            onDismiss = { settingsViewModel.dismissMotivation() }
        )
    }

    shameState?.let { state ->
        ShameOverlay(
            message = state.message,
            photoUri = state.photoUri,
            calorieExcess = state.calorieExcess,
            forced = state.forced,
            countdownSeconds = state.countdownSeconds,
            onDismiss = { settingsViewModel.dismissShame() }
        )
    }
}
