package com.example.kiwi.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
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
import com.example.kiwi.presentation.viewmodel.SocialViewModel
import com.example.kiwi.worker.DownloadWorker

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel,
    onUserClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val pagedSearchResults = viewModel.pagedSearchResults.collectAsLazyPagingItems()
    val searchResultsUsers by socialViewModel.searchResults.collectAsState()

    val dimens = LocalDimens.current
    val context = LocalContext.current
    val isPremiumUser by playerViewModel.isPremium.collectAsState()

    val currentUserId by playerViewModel.currentUserId.collectAsState()

    val filters = listOf(
        "All" to R.string.filter_all,
        "Songs" to R.string.filter_songs,
        "Artists" to R.string.filter_artists,
        "Playlists" to R.string.filter_playlists,
        "Users" to R.string.filter_users
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimens.medium)
    ) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = { newQuery ->
                viewModel.processIntent(SearchIntent.UpdateQuery(newQuery))
                socialViewModel.updateSearchQuery(newQuery)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(id = R.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(id = R.string.search)) },
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
                // --- HISTORY SECTION ---
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
                                        socialViewModel.updateSearchQuery(historyItem.query)
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
                                    Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            } else {
                // --- RESULTS SECTION ---
                LazyColumn(contentPadding = PaddingValues(bottom = dimens.bottomNavPadding)) {

                    // 1. Render Users (if filter is "All" or "Users")
                    if (uiState.currentFilter == "All" || uiState.currentFilter == "Users") {
                        if (searchResultsUsers.isNotEmpty()) {
                            if (uiState.currentFilter == "All") {
                                item {
                                    Text(
                                        text = stringResource(id = R.string.filter_users),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = dimens.small)
                                    )
                                }
                            }
                            items(searchResultsUsers) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onUserClick(user.id) }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = stringResource(id = R.string.profile_picture),
                                            modifier = Modifier.padding(10.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = user.displayName, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = stringResource(id = R.string.followers_count_format, user.username, user.followerCount),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Button(
                                        // FIXED: Added user.isFollowing as the second parameter
                                        onClick = { socialViewModel.toggleFollow(user.id, user.isFollowing) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (user.isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                            contentColor = if (user.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Text(if (user.isFollowing) stringResource(id = R.string.btn_following) else stringResource(id = R.string.btn_follow))
                                    }
                                }
                            }
                        }
                    }

                    // 2. Render Media (if filter is anything EXCEPT "Users")
                    if (uiState.currentFilter != "Users") {
                        if (uiState.currentFilter == "All" && pagedSearchResults.itemCount > 0) {
                            item {
                                Text(
                                    text = stringResource(id = R.string.filter_songs),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = dimens.small)
                                )
                            }
                        }

                        if (pagedSearchResults.loadState.refresh is LoadState.Loading) {
                            items(6) { ShimmerSongItemRow() }
                        } else if (pagedSearchResults.itemCount > 0) {
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
                                                val workData = Data.Builder()
                                                    .putString("SONG_ID", clickedSong.id)
                                                    .putString("AUDIO_URL", clickedSong.audioUrl)
                                                    .putString("SONG_TITLE", clickedSong.title)
                                                    .putString("ARTIST_NAME", clickedSong.artistName)
                                                    .putString("COVER_URL", clickedSong.coverImageUrl)
                                                    .putString("ACCOUNT_ID", currentUserId)
                                                    .build()

                                                val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                                                    .setInputData(workData).build()

                                                WorkManager.getInstance(context).enqueue(downloadRequest)
                                                Toast.makeText(context, context.getString(R.string.downloading_song, clickedSong.title), Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, context.getString(R.string.upgrade_premium_download_offline), Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 3. Empty State Check
                    if (pagedSearchResults.itemCount == 0 && searchResultsUsers.isEmpty() && pagedSearchResults.loadState.refresh !is LoadState.Loading) {
                        item {
                            Text(
                                text = stringResource(id = R.string.no_results_found),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = dimens.large)
                            )
                        }
                    }
                }
            }
        }
    }
}