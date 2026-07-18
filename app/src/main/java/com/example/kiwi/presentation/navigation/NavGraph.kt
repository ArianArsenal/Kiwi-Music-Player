package com.example.kiwi.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.kiwi.presentation.screens.*
import com.example.kiwi.presentation.viewmodel.PlayerViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Downloads : Screen("downloads")
    object Playlists : Screen("playlists")
    object Profile : Screen("profile")
}

@Composable
fun KiwiNavGraph(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) { HomeScreen(playerViewModel = playerViewModel) }
        composable(Screen.Search.route) { SearchScreen() }
        composable(Screen.Downloads.route) { DownloadsScreen() }
        composable(Screen.Playlists.route) { PlaylistsScreen() }
        composable(Screen.Profile.route) { ProfileScreen() }
    }
}