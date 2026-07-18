package com.example.kiwi.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.kiwi.presentation.screens.*
import com.example.kiwi.presentation.viewmodel.PlayerViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Downloads : Screen("downloads")
    object Playlists : Screen("playlists")
    object Profile : Screen("profile")

    object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
        fun createRoute(playlistId: String) = "playlist_detail/$playlistId"
    }
}

@Composable
fun KiwiNavGraph(
    navController: NavHostController,
    playerViewModel: PlayerViewModel, // Accept the ViewModel
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // Pass it down to the Home Screen
        composable(Screen.Home.route) { HomeScreen(playerViewModel = playerViewModel) }
        composable(Screen.Search.route) { SearchScreen(playerViewModel = playerViewModel) }
        composable(Screen.Downloads.route) { DownloadsScreen(playerViewModel = playerViewModel) }
        composable(Screen.Profile.route) { ProfileScreen() }
        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                }
            )
        }
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) {
            PlaylistDetailScreen(
                playerViewModel = playerViewModel,
                onBackClick = { navController.navigateUp() }
            )
        }

    }
}