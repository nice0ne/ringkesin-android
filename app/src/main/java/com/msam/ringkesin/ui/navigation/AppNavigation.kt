package com.msam.ringkesin.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.msam.ringkesin.ui.record.RecordScreen
import com.msam.ringkesin.ui.summary.SummaryScreen
import com.msam.ringkesin.ui.history.HistoryScreen
import com.msam.ringkesin.ui.settings.SettingsScreen
import com.msam.ringkesin.ui.theme.RingkesinTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    onThemeChanged: ((RingkesinTheme) -> Unit) = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val context = LocalContext.current
    val uiLang = context.getSharedPreferences("ringkesin_settings", Context.MODE_PRIVATE)
        .getString("ui_lang", "en") ?: "en"

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.displayName(uiLang)
                                )
                            },
                            label = { Text(screen.displayName(uiLang), fontSize = MaterialTheme.typography.labelSmall.fontSize) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Record.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Record.route) {
                RecordScreen()
            }
            composable(Screen.Summary.route) {
                SummaryScreen()
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onThemeChanged = onThemeChanged)
            }
        }
    }
}
