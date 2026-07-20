package com.example.kiwi.presentation.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiwi.R
import com.example.kiwi.presentation.components.ShimmerSongItemRow
import com.example.kiwi.presentation.components.SongItemRow
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.DownloadsEffect
import com.example.kiwi.presentation.viewmodel.DownloadsIntent
import com.example.kiwi.presentation.viewmodel.DownloadsViewModel
import com.example.kiwi.presentation.viewmodel.PlayerViewModel
import com.example.kiwi.presentation.viewmodel.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val dimens = LocalDimens.current
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DownloadsEffect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimens.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.downloads_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    SortOrder.values().forEach { order ->
                        val labelResId = when (order) {
                            SortOrder.TITLE_ASC -> R.string.sort_title_asc
                            SortOrder.ARTIST_ASC -> R.string.sort_artist_asc
                            SortOrder.NEWEST -> R.string.sort_newest
                        }

                        DropdownMenuItem(
                            text = { Text(stringResource(id = labelResId)) },
                            onClick = {
                                viewModel.processIntent(DownloadsIntent.ChangeSortOrder(order))
                                expanded = false
                            },
                            trailingIcon = if (uiState.sortOrder == order) {
                                { Icon(Icons.Default.Sort, contentDescription = "Active") }
                            } else null
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(dimens.medium))
        if (uiState.isLoading) {
            LazyColumn { items(6) { ShimmerSongItemRow() } }
        } else if (uiState.downloadedSongs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(id = R.string.no_downloaded_songs), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = dimens.bottomNavPadding)) {
                itemsIndexed(
                    items = uiState.downloadedSongs,
                    key = { _, song -> song.id }
                ) { index, song ->

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.processIntent(DownloadsIntent.DeleteSong(song))
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                MaterialTheme.colorScheme.errorContainer
                            } else Color.Transparent

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(end = dimens.medium),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        },
                        content = {
                            Surface(color = MaterialTheme.colorScheme.background) {
                                SongItemRow(
                                    song = song,
                                    isPremium = true, // Already local, button defaults to primary tint
                                    onClick = { playerViewModel.playSongList(uiState.downloadedSongs, index) },
                                    onDownloadClick = { /* Already downloaded, no action needed */ }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}