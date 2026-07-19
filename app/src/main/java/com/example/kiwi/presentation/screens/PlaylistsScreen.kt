package com.example.kiwi.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.kiwi.presentation.components.PlaylistCard
import com.example.kiwi.presentation.components.ShimmerPlaylistCard
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.PlaylistsViewModel
import androidx.compose.ui.res.stringResource
import com.example.kiwi.R
@Composable
fun PlaylistsScreen(
    viewModel: PlaylistsViewModel = hiltViewModel(),
    onPlaylistClick: (String) -> Unit
) {
    val pagedPlaylists = viewModel.pagedPlaylists.collectAsLazyPagingItems()
    val dimens = LocalDimens.current

    // A vibrant palette for our colored cards
    val cardColors = listOf(
        Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
        Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4DB6AC)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(id = R.string.playlists_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(dimens.medium)
        )

        if (pagedPlaylists.loadState.refresh is LoadState.Loading) {
            // Skeleton Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = dimens.medium, vertical = dimens.small),
                horizontalArrangement = Arrangement.spacedBy(dimens.medium),
                verticalArrangement = Arrangement.spacedBy(dimens.medium)
            ) {
                items(8) { ShimmerPlaylistCard() }
            }
        } else {
            val loadedPlaylists = pagedPlaylists.itemSnapshotList.items.filterNotNull()

            // Slice the data into our 3 required categories
            val worldMusic = loadedPlaylists.take(4)
            val localMusic = loadedPlaylists.drop(4).take(4)
            val userPlaylists = loadedPlaylists.drop(8)

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = dimens.medium,
                    end = dimens.medium,
                    bottom = dimens.bottomNavPadding
                ),
                horizontalArrangement = Arrangement.spacedBy(dimens.medium),
                verticalArrangement = Arrangement.spacedBy(dimens.medium)
            ) {
                // CATEGORY 1: World Music
                if (worldMusic.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        CategoryHeader(stringResource(id = R.string.world_music))
                    }
                    itemsIndexed(worldMusic) { index, playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            backgroundColor = cardColors[index % cardColors.size],
                            onClick = { onPlaylistClick(playlist.id) }
                        )
                    }
                }

                // CATEGORY 2: Local Music
                if (localMusic.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        CategoryHeader(stringResource(id = R.string.local_music))
                    }
                    itemsIndexed(localMusic) { index, playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            backgroundColor = cardColors[(index + 2) % cardColors.size],
                            onClick = { onPlaylistClick(playlist.id) }
                        )
                    }
                }

                // CATEGORY 3: User Playlists
                if (userPlaylists.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        CategoryHeader(stringResource(id = R.string.your_playlists))
                    }
                    itemsIndexed(userPlaylists) { index, playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            backgroundColor = cardColors[(index + 4) % cardColors.size],
                            onClick = { onPlaylistClick(playlist.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    val dimens = LocalDimens.current
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = dimens.medium, bottom = dimens.small)
    )
}