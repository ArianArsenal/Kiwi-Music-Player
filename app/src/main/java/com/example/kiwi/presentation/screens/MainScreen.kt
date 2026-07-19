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
import androidx.navigation.compose.rememberNavController
import com.example.kiwi.presentation.components.KiwiBottomNavBar
import com.example.kiwi.presentation.components.KiwiTopBar
import com.example.kiwi.presentation.components.MiniPlayer
import com.example.kiwi.presentation.navigation.KiwiNavGraph
import com.example.kiwi.presentation.theme.KiwiMusicPlayerTheme
import com.example.kiwi.presentation.viewmodel.PlayerViewModel
import com.example.kiwi.presentation.components.ShimmerSongItemRow
@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    var isPlayerExpanded by rememberSaveable { mutableStateOf(false) }

    KiwiMusicPlayerTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = { KiwiTopBar() },
                bottomBar = {
                    Column {
                        MiniPlayer(
                            viewModel = playerViewModel,
                            onExpand = { isPlayerExpanded = true }
                        )
                        KiwiBottomNavBar(navController = navController)
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