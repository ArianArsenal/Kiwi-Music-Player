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
import com.example.kiwi.presentation.theme.KiwiTheme
import com.example.kiwi.presentation.viewmodel.PlayerViewModel

@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    // State to track if the full-screen player is open or closed
    var isPlayerExpanded by rememberSaveable { mutableStateOf(false) }

    KiwiTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Your normal app content
            Scaffold(
                topBar = { KiwiTopBar() },
                bottomBar = {
                    Column {
                        MiniPlayer(
                            viewModel = playerViewModel,
                            onExpand = { isPlayerExpanded = true } // Open player when clicked!
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

            // The Full Screen Player Layer
            AnimatedVisibility(
                visible = isPlayerExpanded,
                enter = slideInVertically(initialOffsetY = { it }), // Slides up from the bottom
                exit = slideOutVertically(targetOffsetY = { it })  // Slides down to close
            ) {
                FullScreenPlayer(
                    viewModel = playerViewModel,
                    onClose = { isPlayerExpanded = false }
                )
            }
        }
    }
}