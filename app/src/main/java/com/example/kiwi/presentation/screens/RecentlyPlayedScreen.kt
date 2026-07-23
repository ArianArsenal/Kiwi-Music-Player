package com.example.kiwi.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiwi.R
import com.example.kiwi.presentation.components.SongItemRow
import com.example.kiwi.presentation.viewmodel.HomeViewModel
import com.example.kiwi.presentation.viewmodel.PlayerIntent
import com.example.kiwi.presentation.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyPlayedScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel
) {
    val recentlyPlayed by viewModel.recentlyPlayedSongs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.recently_played),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back)) // <-- RTL FIX
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recentlyPlayed) { song ->
                SongItemRow(
                    song = song,
                    onClick = {
                        playerViewModel.processIntent(
                            PlayerIntent.PlaySongList(recentlyPlayed, recentlyPlayed.indexOf(song))
                        )
                    }
                )
            }
        }
    }
}