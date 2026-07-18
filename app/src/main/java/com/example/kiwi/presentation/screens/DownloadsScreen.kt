package com.example.kiwi.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiwi.presentation.viewmodel.DownloadsViewModel
import com.example.kiwi.presentation.viewmodel.PlayerViewModel

@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel
) {
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Offline Library",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
        )

        if (downloadedSongs.isEmpty()) {
            Text(
                text = "No songs downloaded yet.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Room for the MiniPlayer
            ) {
                itemsIndexed(downloadedSongs) { index, song ->
                    SongItemRow(
                        song = song,
                        onClick = { playerViewModel.playSongList(downloadedSongs, index) }
                    )
                }
            }
        }
    }
}