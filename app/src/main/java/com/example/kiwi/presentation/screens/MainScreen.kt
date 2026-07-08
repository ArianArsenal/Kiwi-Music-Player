package com.example.kiwi.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.kiwi.presentation.components.KiwiBottomNavBar
import com.example.kiwi.presentation.components.KiwiTopBar
import com.example.kiwi.presentation.navigation.KiwiNavGraph
import com.example.kiwi.presentation.theme.KiwiTheme

@Composable
fun MainScreen() {
    // Initialize the navigation controller that will handle the screen swapping
    val navController = rememberNavController()

    KiwiTheme {
        Scaffold(
            topBar = { KiwiTopBar() },
            bottomBar = { KiwiBottomNavBar(navController = navController) },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->

            // This replaces the placeholder Box. The NavGraph will swap the actual screens in here.
            KiwiNavGraph(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}