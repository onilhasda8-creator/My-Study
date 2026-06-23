package com.studyplanner.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Dashboard   : Screen("dashboard")
    object Subjects    : Screen("subjects")
    object Tasks       : Screen("tasks")
    object Timer       : Screen("timer")
    object Statistics  : Screen("statistics")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard,  "Dashboard",  Icons.Filled.Home),
    BottomNavItem(Screen.Subjects,   "Subjects",   Icons.Filled.Book),
    BottomNavItem(Screen.Tasks,      "Tasks",      Icons.Filled.CheckCircle),
    BottomNavItem(Screen.Timer,      "Timer",      Icons.Filled.Timer),
    BottomNavItem(Screen.Statistics, "Stats",      Icons.Filled.BarChart)
)
