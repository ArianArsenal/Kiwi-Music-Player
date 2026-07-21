package com.example.kiwi.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kiwi.R
import com.example.kiwi.presentation.components.SongItemRow
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.PlayerViewModel
import com.example.kiwi.presentation.viewmodel.PlaylistDetailViewModel
import com.example.kiwi.worker.DownloadWorker

@Composable
fun PlaylistDetailScreen(
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagedSongs = viewModel.pagedPlaylistSongs.collectAsLazyPagingItems()
    val dimens = LocalDimens.current
    val context = LocalContext.current

    // Toggle this to test both standard and premium barriers
    val isPremiumUser by playerViewModel.isPremium.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimens.medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = dimens.medium, bottom = dimens.large)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.content_desc_go_back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(id = R.string.title_playlist_tracks),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = dimens.small)
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (pagedSongs.loadState.refresh is LoadState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (pagedSongs.itemCount == 0) {
                Text(
                    text = stringResource(id = R.string.status_empty_playlist),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = dimens.bottomNavPadding)
                ) {
                    items(count = pagedSongs.itemCount) { index ->
                        val song = pagedSongs[index]
                        if (song != null) {
                            SongItemRow(
                                song = song,
                                isPremium = isPremiumUser,
                                onClick = {
                                    val loadedSongs = pagedSongs.itemSnapshotList.items.filterNotNull()
                                    playerViewModel.playSongList(loadedSongs, index)
                                },
                                onDownloadClick = { clickedSong ->
                                    if (isPremiumUser) {
                                        val workData = androidx.work.Data.Builder()
                                            .putString("SONG_ID", clickedSong.id)
                                            .putString("AUDIO_URL", clickedSong.audioUrl)
                                            .putString("SONG_TITLE", clickedSong.title)
                                            .putString("ARTIST_NAME", clickedSong.artistName)
                                            .putString("COVER_URL", clickedSong.coverImageUrl)
                                            .build()

                                        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                                            .setInputData(workData)
                                            .build()

                                        WorkManager.getInstance(context).enqueue(downloadRequest)
                                        Toast.makeText(context, "Downloading ${clickedSong.title}...", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Upgrade to Premium to download songs offline!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        }
                    }

                    if (pagedSongs.loadState.append is LoadState.Loading) {
                        item {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimens.medium)
                                    .wrapContentWidth(Alignment.CenterHorizontally),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}