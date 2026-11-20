package ru.habittracker.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.habittracker.HabitApp
import ru.habittracker.ui.screens.CalendarScreen
import ru.habittracker.ui.screens.HabitEditScreen
import ru.habittracker.ui.screens.HabitsScreen
import ru.habittracker.ui.screens.SettingsScreen
import ru.habittracker.ui.screens.TodayScreen
import ru.habittracker.viewmodel.HabitViewModel
import ru.habittracker.viewmodel.HabitViewModelFactory

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Today : Screen("today", "Сегодня", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle)
    data object Habits : Screen("habits", "Привычки", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List)
    data object Calendar : Screen("calendar", "Календарь", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    data object Settings : Screen("settings", "Настройки", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun HabitNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as HabitApp
    val viewModel: HabitViewModel = viewModel(
        factory = HabitViewModelFactory(app.repository)
    )

    val items = listOf(Screen.Today, Screen.Habits, Screen.Calendar, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in items.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars
                ) {
                    items.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) screen.selectedIcon else screen.unselectedIcon,
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Today.route) {
                TodayScreen(
                    viewModel = viewModel,
                    onAddHabit = { navController.navigate("edit/0") },
                    onEditHabit = { id -> navController.navigate("edit/$id") }
                )
            }
            composable(Screen.Habits.route) {
                HabitsScreen(
                    viewModel = viewModel,
                    onAddHabit = { navController.navigate("edit/0") },
                    onEditHabit = { id -> navController.navigate("edit/$id") }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(userPrefs = app.userPrefs)
            }
            composable("edit/{habitId}") { entry ->
                val id = entry.arguments?.getString("habitId")?.toLongOrNull() ?: 0L
                HabitEditScreen(
                    habitId = id,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
