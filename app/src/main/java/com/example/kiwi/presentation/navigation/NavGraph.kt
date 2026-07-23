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
    object Settings : Screen("settings")
    object Auth : Screen("auth_screen")

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
        startDestination = Screen.Auth.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                playerViewModel = playerViewModel,
                onLikedSongsClick = { navController.navigate("liked_songs") },
                onPlaylistsClick = { navController.navigate(Screen.Playlists.route) },
                onRecentlyPlayedClick = { navController.navigate("recently_played") },
                onTopArtistsClick = { navController.navigate("top_artists") }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                playerViewModel = playerViewModel,
                onUserClick = { userId ->
                    navController.navigate("user_profile/$userId")
                }
            )
        }

        composable(Screen.Downloads.route) { DownloadsScreen(playerViewModel = playerViewModel) }

        composable("social_search_screen") {
            com.example.kiwi.presentation.screens.SocialSearchScreen(navController = navController)
        }

        composable("user_profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            UserProfileScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onPlaylistClick = { playlistId -> navController.navigate("playlist_detail/$playlistId") },
                onMessageClick = { id, name -> navController.navigate("chat_screen/$id/$name") },
                onFollowersClick = { uid -> navController.navigate("connections/$uid/followers") },
                onFollowingClick = { uid -> navController.navigate("connections/$uid/following") }
            )
        }

        composable(
            route = "connections/{userId}/{type}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("userId") ?: ""
            val type = backStackEntry.arguments?.getString("type") ?: "followers"

            com.example.kiwi.presentation.screens.UserConnectionsScreen(
                userId = uid,
                connectionType = type,
                onBackClick = { navController.popBackStack() },
                onUserClick = { clickedUserId -> navController.navigate("user_profile/$clickedUserId") }
            )
        }

        composable("recently_played") {
            com.example.kiwi.presentation.screens.RecentlyPlayedScreen(
                onBackClick = { navController.popBackStack() },
                playerViewModel = playerViewModel
            )
        }

        composable("top_artists") {
            com.example.kiwi.presentation.screens.TopArtistsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                },
                onBackClick = { navController.popBackStack() } // <-- ADDED: Navigates back if opened from Home
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onFindFriendsClick = { navController.navigate("social_search_screen") },
                onLikedSongsClick = { navController.navigate("liked_songs") },
                onFollowersClick = { uid -> navController.navigate("connections/$uid/followers") },
                onFollowingClick = { uid -> navController.navigate("connections/$uid/following") },
                onRecentlyPlayedClick = { navController.navigate("recently_played") },
                onTopArtistsClick = { navController.navigate("top_artists") },
                onUsernameClick = { uid -> navController.navigate("user_profile/$uid") },
                onMyPlaylistsClick = { navController.navigate(Screen.Playlists.route) }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: com.example.kiwi.presentation.viewmodel.SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    settingsViewModel.logOutUser()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable("liked_songs") {
            com.example.kiwi.presentation.screens.LikedSongsScreen(
                onPlayAll = { songs, startIndex ->
                    playerViewModel.processIntent(
                        com.example.kiwi.presentation.viewmodel.PlayerIntent.PlaySongList(songs, startIndex)
                    )
                },
                onBackClick = { navController.popBackStack() }
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

        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable("inbox") {
            com.example.kiwi.presentation.screens.InboxScreen(
                onBackClick = { navController.popBackStack() },
                onChatClick = { friendId, friendName ->
                    navController.navigate("chat_screen/$friendId/$friendName")
                },
                onSearchClick = { navController.navigate("social_search_screen") }
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

            val localPlayerViewModel: com.example.kiwi.presentation.viewmodel.PlayerViewModel =
                androidx.hilt.navigation.compose.hiltViewModel(LocalContext.current as androidx.activity.ComponentActivity)

            com.example.kiwi.presentation.screens.ChatScreen(
                friendId = friendId,
                friendName = friendName,
                playerViewModel = localPlayerViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}