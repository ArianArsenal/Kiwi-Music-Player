package com.example.kiwi.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.kiwi.presentation.screens.*
import com.example.kiwi.presentation.viewmodel.PlayerViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Downloads : Screen("downloads")
    object Playlists : Screen("playlists")
    object Profile : Screen("profile")

    // NEW: Settings Route
    object Settings : Screen("settings")

    object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
        fun createRoute(playlistId: String) = "playlist_detail/$playlistId"
    }
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
        composable(Screen.Search.route) { SearchScreen(playerViewModel = playerViewModel) }
        composable(Screen.Downloads.route) { DownloadsScreen(playerViewModel = playerViewModel) }

        composable("social_search_screen") {
            com.example.kiwi.presentation.screens.SocialSearchScreen(navController = navController)
        }

        composable("user_profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable

            com.example.kiwi.presentation.screens.UserProfileScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onPlaylistClick = { playlistId ->
                    navController.navigate("playlist_detail/$playlistId")
                },
                onMessageClick = { id, name ->
                    // Navigates directly into the chat with the dynamic user!
                    navController.navigate("chat_screen/$id/$name")
                }
            )
        }

        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                }
            )
        }

        composable(Screen.Profile.route) {
            // FIXED: Passing the onSettingsClick and onFindFriendsClick parameters
            ProfileScreen(
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onFindFriendsClick = { navController.navigate("social_search_screen") } // <-- NEW NAVIGATION
            )
        }

        // NEW: Settings Screen UI
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.navigateUp() }
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

        composable("inbox") {
            com.example.kiwi.presentation.screens.InboxScreen(
                onBackClick = { navController.popBackStack() },
                onChatClick = { friendId, friendName ->
                    // When a row is clicked, it routes to the dynamic chat screen!
                    navController.navigate("chat_screen/$friendId/$friendName")
                }
            )
        }

        composable(
            route = "chat_screen/{friendId}/{friendName}",
            arguments = listOf(
                navArgument("friendId") { type = NavType.StringType },
                navArgument("friendName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            val friendName = backStackEntry.arguments?.getString("friendName") ?: "Friend"

            val playerViewModel: com.example.kiwi.presentation.viewmodel.PlayerViewModel =
                androidx.hilt.navigation.compose.hiltViewModel(LocalContext.current as androidx.activity.ComponentActivity)

            com.example.kiwi.presentation.screens.ChatScreen(
                friendId = friendId,
                friendName = friendName,
                playerViewModel = playerViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}