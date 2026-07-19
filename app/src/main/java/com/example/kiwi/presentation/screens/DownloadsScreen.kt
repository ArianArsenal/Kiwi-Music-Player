package com.example.kiwi.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiwi.R
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.DownloadsViewModel
import com.example.kiwi.presentation.viewmodel.PlayerViewModel

@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel
) {
    val uiState by viewModel.uiState.collectAsState() // CHANGED
    val dimens = LocalDimens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimens.medium)
    ) {
        Text(
            text = stringResource(id = R.string.title_offline_library),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = dimens.medium, bottom = dimens.large)
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else if (uiState.downloadedSongs.isEmpty()) { // CHANGED
            Text(
                text = stringResource(id = R.string.status_no_downloads),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = dimens.bottomNavPadding)
            ) {
                itemsIndexed(uiState.downloadedSongs) { index, song -> // CHANGED
                    SongItemRow(
                        song = song,
                        onClick = { playerViewModel.playSongList(uiState.downloadedSongs, index) }
                    )
                }
            }
        }
    }
}