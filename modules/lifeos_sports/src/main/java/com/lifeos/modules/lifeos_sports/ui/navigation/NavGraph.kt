package com.lifeos.modules.lifeos_sports.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Schedule : Screen("schedule", "Schedule", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    data object Standings : Screen("standings", "Standings", Icons.Filled.FormatListNumbered, Icons.Outlined.FormatListNumbered)
    data object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object LastGame : Screen("last_game", "Last Game", Icons.Filled.History, Icons.Outlined.History)
    data object Settings : Screen("settings", "My Teams", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val bottomNavItems = listOf(
    Screen.Schedule,
    Screen.Standings,
    Screen.Home,
    Screen.LastGame,
    Screen.Settings
)
