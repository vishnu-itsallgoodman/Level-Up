package com.levelup.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.levelup.app.ui.screens.achievements.AchievementsScreen
import com.levelup.app.ui.screens.history.HistoryScreen
import com.levelup.app.ui.screens.home.HomeScreen
import com.levelup.app.ui.screens.settings.SettingsScreen
import com.levelup.app.ui.screens.stats.StatsScreen
import com.levelup.app.ui.theme.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home         : Screen("home",         "Home",     Icons.Default.Home)
    object History      : Screen("history",      "History",  Icons.Default.DateRange)
    object Stats        : Screen("stats",        "Stats",    Icons.Default.BarChart)
    object Achievements : Screen("achievements", "Awards",   Icons.Default.EmojiEvents)
    object Settings     : Screen("settings",     "Settings", Icons.Default.Settings)
}

private val navItems = listOf(
    Screen.Home, Screen.History, Screen.Stats, Screen.Achievements, Screen.Settings
)

@Composable
fun MainNavGraph() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDest = navBackStackEntry?.destination
                navItems.forEach { screen ->
                    val selected = currentDest?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(screen.icon, contentDescription = screen.label,
                                tint = if (selected) AccentBlue else TextMuted)
                        },
                        label = {
                            Text(screen.label, fontSize = 10.sp,
                                color = if (selected) AccentBlue else TextMuted)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AccentBlue.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route)         { HomeScreen() }
            composable(Screen.History.route)      { HistoryScreen() }
            composable(Screen.Stats.route)        { StatsScreen() }
            composable(Screen.Achievements.route) { AchievementsScreen() }
            composable(Screen.Settings.route)     { SettingsScreen() }
        }
    }
}
