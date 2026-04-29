package com.lifeos.modules.lifeos_mealtracker

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import com.lifeos.core.LifeOSModule
import com.lifeos.modules.lifeos_mealtracker.ui.addmeal.AddMealScreen
import com.lifeos.modules.lifeos_mealtracker.ui.analytics.AnalyticsScreen
import com.lifeos.modules.lifeos_mealtracker.ui.dashboard.DashboardScreen
import com.lifeos.modules.lifeos_mealtracker.ui.navigation.Screen
import com.lifeos.modules.lifeos_mealtracker.ui.navigation.bottomNavItems
import com.lifeos.modules.lifeos_mealtracker.ui.savedmeals.SavedMealsScreen
import com.lifeos.modules.lifeos_mealtracker.ui.settings.SettingsScreen
import com.lifeos.modules.lifeos_mealtracker.ui.settings.SettingsViewModel
import com.lifeos.modules.lifeos_mealtracker.ui.shame.MotivationOverlay
import com.lifeos.modules.lifeos_mealtracker.ui.shame.ShameOverlay
import com.lifeos.modules.lifeos_mealtracker.ui.storeditems.AddStoredItemScreen
import com.lifeos.modules.lifeos_mealtracker.ui.theme.BrutalMealTrackerTheme
import com.lifeos.modules.lifeos_mealtracker.ui.weight.WeightScreen

class MealTrackerModule : LifeOSModule {
    override val id: String = "mealtracker"
    override val name: String = "Brutal Meal Tracker"
    override val icon: ImageVector = Icons.Filled.Restaurant
    override val description: String = "Track meals, calories, macros, and weight with brutal honesty"
    override val shortDescription: String = "Meals, calories & macros"
    override val version: String = "1.0"

    @Composable
    override fun Content(
        onNavigateBack: () -> Unit,
        initialId: Long?
    ) {
        val moduleNavController = rememberNavController()
        val settingsViewModel: SettingsViewModel = hiltViewModel()

        val shameState by settingsViewModel.shameState.collectAsState()
        val motivationState by settingsViewModel.motivationState.collectAsState()
        val settings by settingsViewModel.settings.collectAsState()

        BrutalMealTrackerTheme(darkTheme = settings.isDarkMode) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to LifeOS",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Back to LifeOS",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    ModuleContent(
                        moduleNavController = moduleNavController,
                        settingsViewModel = settingsViewModel
                    )
                }

                NavigationBar {
                    val navBackStackEntry by moduleNavController.currentBackStackEntryAsState()
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
                                moduleNavController.navigate(screen.route) {
                                    popUpTo(moduleNavController.graph.findStartDestination().id) {
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
    }

    @Composable
    private fun ModuleContent(
        moduleNavController: androidx.navigation.NavHostController,
        settingsViewModel: SettingsViewModel
    ) {
        val context = LocalContext.current

        val notificationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                settingsViewModel.updateHourlyShameNotifications(true)
            }
        }

        NavHost(
            navController = moduleNavController,
            startDestination = Screen.Dashboard.route
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onAddMeal = { moduleNavController.navigate("add_meal") },
                    onEditMeal = { mealId -> moduleNavController.navigate("edit_meal/$mealId") }
                )
            }
            composable("add_meal") {
                AddMealScreen(
                    mealId = null,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onShowShame = { calorieExcess ->
                        settingsViewModel.showShameOverlay(calorieExcess)
                    }
                )
            }
            composable(
                route = "edit_meal/{mealId}",
                arguments = listOf(navArgument("mealId") { type = NavType.LongType })
            ) { backStackEntry ->
                val mealId = backStackEntry.arguments?.getLong("mealId")
                AddMealScreen(
                    mealId = mealId,
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onShowShame = { calorieExcess ->
                        settingsViewModel.showShameOverlay(calorieExcess)
                    }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }
            composable(Screen.SavedMeals.route) {
                SavedMealsScreen(
                    onMealSelected = { savedMeal ->
                        moduleNavController.navigate("add_meal?savedMealId=${savedMeal.id}")
                    },
                    onStoredItemSelected = { storedItem, quantity ->
                        moduleNavController.navigate("add_meal?storedItemId=${storedItem.id}&storedItemQuantity=$quantity")
                    },
                    onAddStoredItem = {
                        moduleNavController.navigate("add_stored_item")
                    },
                    onEditStoredItem = { itemId ->
                        moduleNavController.navigate("edit_stored_item/$itemId")
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
                    onNavigateBack = { moduleNavController.popBackStack() },
                    onShowShame = { calorieExcess ->
                        settingsViewModel.showShameOverlay(calorieExcess)
                    }
                )
            }
            composable("add_stored_item") {
                AddStoredItemScreen(
                    itemId = null,
                    onNavigateBack = { moduleNavController.popBackStack() }
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
                    onNavigateBack = { moduleNavController.popBackStack() }
                )
            }
            composable(Screen.Weight.route) {
                WeightScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNotificationPermissionRequired = {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                )
            }
        }
    }
}
