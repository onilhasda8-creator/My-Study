package com.studyplanner

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.studyplanner.navigation.Screen
import com.studyplanner.navigation.bottomNavItems
import com.studyplanner.ui.screens.dashboard.DashboardScreen
import com.studyplanner.ui.screens.statistics.StatisticsScreen
import com.studyplanner.ui.screens.subjects.SubjectsScreen
import com.studyplanner.ui.screens.tasks.TasksScreen
import com.studyplanner.ui.screens.timer.StudyTimerScreen

@Composable
fun StudyPlannerApp() {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStack?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy
                            ?.any { it.route == item.screen.route } == true,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
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
            composable(Screen.Dashboard.route)   { DashboardScreen() }
            composable(Screen.Subjects.route)    { SubjectsScreen() }
            composable(Screen.Tasks.route)       { TasksScreen() }
            composable(Screen.Timer.route)       { StudyTimerScreen() }
            composable(Screen.Statistics.route)  { StatisticsScreen() }
        }
    }
}
