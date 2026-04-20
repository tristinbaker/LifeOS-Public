package com.lifeos.modules.lifeos_sports

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lifeos.core.LifeOSModule
import com.lifeos.modules.lifeos_sports.ui.lastgame.LastGameScreen
import com.lifeos.modules.lifeos_sports.ui.navigation.Screen
import com.lifeos.modules.lifeos_sports.ui.navigation.bottomNavItems
import com.lifeos.modules.lifeos_sports.ui.schedule.ScheduleScreen
import com.lifeos.modules.lifeos_sports.ui.scores.ScoresScreen
import com.lifeos.modules.lifeos_sports.ui.settings.SettingsScreen
import com.lifeos.modules.lifeos_sports.ui.standings.StandingsScreen

class SportsModule : LifeOSModule {
    override val id: String = "sports"
    override val name: String = "Sports"
    override val icon: ImageVector = Icons.Filled.SportsSoccer
    override val description: String = "Scores, standings, and last game recaps for your favorite teams"
    override val version: String = "1.0"

    @Composable
    override fun Content(
        onNavigateBack: () -> Unit,
        initialId: Long?
    ) {
        val moduleNavController = rememberNavController()
        var pendingLastGameTeamId by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
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
                NavHost(
                    navController = moduleNavController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Schedule.route) { ScheduleScreen() }
                    composable(Screen.Standings.route) { StandingsScreen() }
                    composable(Screen.Home.route) {
                        ScoresScreen(
                            onGameClick = { teamId ->
                                pendingLastGameTeamId = teamId
                                moduleNavController.navigate(Screen.LastGame.route) {
                                    popUpTo(moduleNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    composable(Screen.LastGame.route) {
                        LastGameScreen(
                            initialTeamId = pendingLastGameTeamId,
                            onScrolled = { pendingLastGameTeamId = null }
                        )
                    }
                    composable(Screen.Settings.route) { SettingsScreen() }
                }
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
    }
}
