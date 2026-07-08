package com.example.kiwi.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kiwi.R
import com.example.kiwi.presentation.navigation.Screen

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int
)

@Composable
fun KiwiBottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(Screen.Home.route, Icons.Default.Home, R.string.tab_home),
        BottomNavItem(Screen.Search.route, Icons.Default.Search, R.string.tab_search),
        BottomNavItem(Screen.Downloads.route, Icons.Default.KeyboardArrowDown, R.string.tab_downloads),
        BottomNavItem(Screen.Playlists.route, Icons.AutoMirrored.Filled.List, R.string.tab_playlists),
        BottomNavItem(Screen.Profile.route, Icons.Default.Person, R.string.tab_profile)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // Observe the current back stack to determine which tab should be selected
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = stringResource(id = item.labelResId)) },
                label = { Text(stringResource(id = item.labelResId)) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination to avoid building a huge stack of screens
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        // Avoid multiple copies of the same screen on the back stack
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected tab
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.background,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}