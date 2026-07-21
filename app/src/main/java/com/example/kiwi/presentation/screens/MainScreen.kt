package com.example.kiwi.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kiwi.presentation.components.KiwiBottomNavBar
import com.example.kiwi.presentation.components.KiwiTopBar
import com.example.kiwi.presentation.components.MiniPlayer
import com.example.kiwi.presentation.navigation.KiwiNavGraph
import com.example.kiwi.presentation.theme.KiwiMusicPlayerTheme
import com.example.kiwi.presentation.viewmodel.PlayerIntent
import com.example.kiwi.presentation.viewmodel.PlayerViewModel

@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    var isPlayerExpanded by rememberSaveable { mutableStateOf(false) }

    // 1. Collect the player state from the ViewModel
    val playerState by playerViewModel.uiState.collectAsState()

    // 2. Track the current route to determine visibility of system bars
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isImmersiveScreen = currentRoute?.startsWith("chat_screen") == true ||
            currentRoute == "inbox" ||
            currentRoute?.startsWith("user_profile") == true ||
            currentRoute == "social_search_screen"

    KiwiMusicPlayerTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    if (!isImmersiveScreen) {
                        KiwiTopBar(
                            onChatClick = { navController.navigate("inbox") },
                        )
                    }
                },
                bottomBar = {
                    if (!isImmersiveScreen) {
                        Column {
                            // Only show the MiniPlayer if a song is currently loaded
                            playerState.currentSong?.let { song ->
                                MiniPlayer(
                                    song = song,
                                    isPlaying = playerState.isPlaying,
                                    progress = playerState.progress,
                                    onPlayPauseClick = { playerViewModel.processIntent(PlayerIntent.TogglePlayPause) },
                                    onNextClick = { playerViewModel.processIntent(PlayerIntent.SkipNext) },
                                    onNavigateToPlayer = { isPlayerExpanded = true }
                                )
                            }

                            KiwiBottomNavBar(navController = navController)
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                KiwiNavGraph(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                )
            }

            // 3. Your FullScreenPlayer overlay setup remains perfectly intact
            AnimatedVisibility(
                visible = isPlayerExpanded,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                FullScreenPlayer(
                    viewModel = playerViewModel,
                    onClose = { isPlayerExpanded = false }
                )
            }
        }
    }
}