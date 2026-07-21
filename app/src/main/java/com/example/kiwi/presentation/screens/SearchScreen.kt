package com.example.kiwi.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kiwi.R
import com.example.kiwi.presentation.components.ShimmerSongItemRow
import com.example.kiwi.presentation.components.SongItemRow
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.PlayerViewModel
import com.example.kiwi.presentation.viewmodel.SearchIntent
import com.example.kiwi.presentation.viewmodel.SearchViewModel
import com.example.kiwi.worker.DownloadWorker

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val pagedSearchResults = viewModel.pagedSearchResults.collectAsLazyPagingItems()
    val dimens = LocalDimens.current
    val context = LocalContext.current

    val isPremiumUser by playerViewModel.isPremium.collectAsState()

    val filters = listOf(
        "All" to R.string.filter_all,
        "Songs" to R.string.filter_songs,
        "Artists" to R.string.filter_artists,
        "Playlists" to R.string.filter_playlists
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimens.medium)
    ) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = { viewModel.processIntent(SearchIntent.UpdateQuery(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(id = R.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(dimens.medium),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { viewModel.processIntent(SearchIntent.SaveQuery(uiState.query)) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(dimens.small))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(dimens.small)) {
            items(filters) { (filterKey, labelResId) ->
                FilterChip(
                    selected = uiState.currentFilter == filterKey,
                    onClick = { viewModel.processIntent(SearchIntent.UpdateFilter(filterKey)) },
                    label = { Text(stringResource(id = labelResId)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(dimens.medium))

        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.query.isEmpty()) {
                if (recentSearches.isNotEmpty()) {
                    LazyColumn {
                        item {
                            Text(
                                text = stringResource(id = R.string.recent_searches),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = dimens.small)
                            )
                        }
                        items(recentSearches) { historyItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.processIntent(SearchIntent.UpdateQuery(historyItem.query))
                                    }
                                    .padding(vertical = dimens.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(dimens.medium))
                                Text(
                                    text = historyItem.query,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                IconButton(onClick = { viewModel.processIntent(SearchIntent.DeleteHistoryItem(historyItem)) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            } else {
                if (pagedSearchResults.loadState.refresh is LoadState.Loading) {
                    LazyColumn(contentPadding = PaddingValues(bottom = dimens.bottomNavPadding)) {
                        items(6) { ShimmerSongItemRow() }
                    }
                } else if (pagedSearchResults.itemCount == 0) {
                    Text(text = stringResource(id = R.string.no_results_found), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = dimens.bottomNavPadding)) {
                        items(count = pagedSearchResults.itemCount) { index ->
                            pagedSearchResults[index]?.let { song ->
                                SongItemRow(
                                    song = song,
                                    isPremium = isPremiumUser,
                                    onClick = {
                                        viewModel.processIntent(SearchIntent.SaveQuery(song.title))
                                        val loadedSongs = pagedSearchResults.itemSnapshotList.items.filterNotNull()
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
                    }
                }
            }
        }
    }
}