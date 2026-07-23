package com.example.kiwi.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.example.kiwi.R
import com.example.kiwi.presentation.components.SongItemRow
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.PlayerViewModel
import com.example.kiwi.presentation.viewmodel.PlaylistDetailViewModel
import com.example.kiwi.worker.DownloadWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagedSongs = viewModel.pagedPlaylistSongs.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState(initial = emptyList())
    val currentPlaylist by viewModel.currentPlaylist.collectAsState()

    val currentUserId by viewModel.currentUserId.collectAsState()

    val dimens = LocalDimens.current
    val context = LocalContext.current

    val isPremiumUser by playerViewModel.isPremium.collectAsState()
    var showAddSongDialog by remember { mutableStateOf(false) }

    if (showAddSongDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddSongDialog = false
                viewModel.onSearchQueryChange("")
            },
            title = { Text(stringResource(id = R.string.add_songs_to_playlist), fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        label = { Text(stringResource(id = R.string.search_title_or_artist)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                        Text(
                            stringResource(id = R.string.no_results_found),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(searchResults) { song ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.addSongToPlaylist(song.id) {
                                                Toast.makeText(context, context.getString(R.string.added_song_format, song.title), Toast.LENGTH_SHORT).show()
                                                pagedSongs.refresh()
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = song.coverImageUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(song.artistName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add), tint = Color(0xFF4CAF50))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showAddSongDialog = false
                    viewModel.onSearchQueryChange("")
                }) {
                    Text(stringResource(id = R.string.done))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimens.medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimens.medium, bottom = dimens.large)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

            val isMyPlaylist = currentPlaylist?.category?.replace("'", "") == "user" && currentPlaylist?.userId == currentUserId

            if (isMyPlaylist) {
                IconButton(onClick = { showAddSongDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.add_songs),
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
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
                                        val workData = Data.Builder()
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
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.downloading_song, clickedSong.title),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.upgrade_premium_download_offline),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}