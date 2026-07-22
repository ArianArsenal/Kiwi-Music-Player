//package com.example.kiwi.presentation.screens
//
//import androidx.appcompat.app.AppCompatDelegate
//import androidx.core.os.LocaleListCompat
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.runtime.*
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.ui.Modifier
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.navigation.compose.rememberNavController
//import com.example.kiwi.presentation.components.KiwiBottomNavBar
//import com.example.kiwi.presentation.components.KiwiTopBar
//import com.example.kiwi.presentation.components.MiniPlayer
//import com.example.kiwi.presentation.navigation.KiwiNavGraph
//import com.example.kiwi.presentation.navigation.Screen
//import com.example.kiwi.presentation.theme.KiwiMusicPlayerTheme
//import com.example.kiwi.presentation.viewmodel.PlayerIntent
//import com.example.kiwi.presentation.viewmodel.PlayerViewModel
//import com.example.kiwi.presentation.viewmodel.SettingsViewModel
//
//@Composable
//fun MainScreen(
//    playerViewModel: PlayerViewModel = hiltViewModel(),
//    settingsViewModel: SettingsViewModel = hiltViewModel() // <-- NEW: Inject Settings
//) {
//    val navController = rememberNavController()
//    var isPlayerExpanded by rememberSaveable { mutableStateOf(false) }
//
//    val playerState by playerViewModel.uiState.collectAsState()
//
//    // 1. Collect the global settings state
//    val settingsState by settingsViewModel.settingsState.collectAsState()
//
//    // 2. Apply Language & RTL Layout Change dynamically
//    // This tells the Android OS to instantly switch the app's locale configuration
//    LaunchedEffect(settingsState.language) {
//        val localeList = LocaleListCompat.forLanguageTags(settingsState.language)
//        AppCompatDelegate.setApplicationLocales(localeList)
//    }
//
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentRoute = navBackStackEntry?.destination?.route
//
//    val isAuthScreen = currentRoute == "auth_screen"
//
//    val isImmersiveScreen = currentRoute?.startsWith("chat_screen") == true ||
//            currentRoute == "inbox" ||
//            currentRoute?.startsWith("user_profile") == true ||
//            currentRoute == "social_search_screen"
//
//    val shouldShowBars = !isAuthScreen && !isImmersiveScreen
//
//    // 3. Force the theme to obey your DataStore toggle instead of the System default
//    KiwiMusicPlayerTheme(darkTheme = settingsState.isDarkMode) {
//        Box(modifier = Modifier.fillMaxSize()) {
//            Scaffold(
//                topBar = {
//                    if (shouldShowBars) {
//                        KiwiTopBar(
//                            profilePicUri = settingsState.profilePicUri,
//                            onChatClick = { navController.navigate("inbox") },
//                            onSettingsClick = { navController.navigate(Screen.Settings.route) },
//                            onProfileClick = { navController.navigate(Screen.Profile.route) },
//                        )
//                    }
//                },
//                bottomBar = {
//                    if (shouldShowBars) {
//                        Column {
//                            playerState.currentSong?.let { song ->
//                                MiniPlayer(
//                                    song = song,
//                                    isPlaying = playerState.isPlaying,
//                                    progress = playerState.progress,
//                                    onPlayPauseClick = { playerViewModel.processIntent(PlayerIntent.TogglePlayPause) },
//                                    onPreviousClick = { playerViewModel.processIntent(PlayerIntent.SkipPrevious) },
//                                    onNextClick = { playerViewModel.processIntent(PlayerIntent.SkipNext) },
//                                    onNavigateToPlayer = { isPlayerExpanded = true }
//                                )
//                            }
//                            KiwiBottomNavBar(navController = navController)
//                        }
//                    }
//                },
//                containerColor = MaterialTheme.colorScheme.background
//            ) { innerPadding ->
//                KiwiNavGraph(
//                    navController = navController,
//                    playerViewModel = playerViewModel,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(innerPadding)
//                        .background(MaterialTheme.colorScheme.background)
//                )
//            }
//
//            AnimatedVisibility(
//                visible = isPlayerExpanded,
//                enter = slideInVertically(initialOffsetY = { it }),
//                exit = slideOutVertically(targetOffsetY = { it })
//            ) {
//                FullScreenPlayer(
//                    viewModel = playerViewModel,
//                    onClose = { isPlayerExpanded = false }
//                )
//            }
//        }
//    }
//}


package com.example.kiwi.presentation.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
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
import com.example.kiwi.presentation.navigation.Screen
import com.example.kiwi.presentation.theme.KiwiMusicPlayerTheme
import com.example.kiwi.presentation.viewmodel.PlayerIntent
import com.example.kiwi.presentation.viewmodel.PlayerViewModel
import com.example.kiwi.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalSharedTransitionApi::class) // <-- FIXED
@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    var isPlayerExpanded by rememberSaveable { mutableStateOf(false) }

    val playerState by playerViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.settingsState.collectAsState()

    LaunchedEffect(settingsState.language) {
        val localeList = LocaleListCompat.forLanguageTags(settingsState.language)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isAuthScreen = currentRoute == "auth_screen"
    val isImmersiveScreen = currentRoute?.startsWith("chat_screen") == true ||
            currentRoute == "inbox" ||
            currentRoute?.startsWith("user_profile") == true ||
            currentRoute == "social_search_screen"

    val shouldShowBars = !isAuthScreen && !isImmersiveScreen

    KiwiMusicPlayerTheme(darkTheme = settingsState.isDarkMode) {
        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {

            AnimatedContent(
                targetState = isPlayerExpanded,
                label = "player_transition"
            ) { targetExpanded ->
                if (targetExpanded) {
                    FullScreenPlayer(
                        viewModel = playerViewModel,
                        onClose = { isPlayerExpanded = false },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent
                    )
                } else {
                    Scaffold(
                        topBar = {
                            if (shouldShowBars) {
                                KiwiTopBar(
                                    profilePicUri = settingsState.profilePicUri,
                                    onChatClick = { navController.navigate("inbox") },
                                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                                )
                            }
                        },
                        bottomBar = {
                            if (shouldShowBars) {
                                Column {
                                    playerState.currentSong?.let { song ->
                                        MiniPlayer(
                                            song = song,
                                            isPlaying = playerState.isPlaying,
                                            progress = playerState.progress,
                                            onPlayPauseClick = { playerViewModel.processIntent(PlayerIntent.TogglePlayPause) },
                                            onPreviousClick = { playerViewModel.processIntent(PlayerIntent.SkipPrevious) },
                                            onNextClick = { playerViewModel.processIntent(PlayerIntent.SkipNext) },
                                            onNavigateToPlayer = { isPlayerExpanded = true },
                                            sharedTransitionScope = this@SharedTransitionLayout,
                                            animatedVisibilityScope = this@AnimatedContent
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
                }
            }
        }
    }
}