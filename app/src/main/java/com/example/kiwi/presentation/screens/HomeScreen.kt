package com.example.kiwi.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.kiwi.R
import com.example.kiwi.domain.model.Song
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.HomeEffect
import com.example.kiwi.presentation.viewmodel.HomeIntent
import com.example.kiwi.presentation.viewmodel.HomeViewModel
import com.example.kiwi.presentation.viewmodel.PlayerViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // THIS is the magic Paging 3 function!
    val pagedSongs = viewModel.pagedSongs.collectAsLazyPagingItems()

    val dimens = LocalDimens.current
    val context = LocalContext.current

    // 2. Handle one-time Channel effects safely
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowToast -> {
                    Toast.makeText(context, context.getString(effect.messageResId), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // We check Paging 3's built-in state to know when to show the initial loading spinner
        if (pagedSongs.loadState.refresh is LoadState.Loading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimens.medium),
                contentPadding = PaddingValues(top = dimens.medium, bottom = dimens.bottomNavPadding)
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.title_featured_tracks),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = dimens.medium)
                    )
                }

                // Paging 3 uses item count, not a direct list
                items(count = pagedSongs.itemCount) { index ->
                    val song = pagedSongs[index]
                    // Paging can insert null placeholders, so we must check for null
                    if (song != null) {
                        SongItemRow(
                            song = song,
                            onClick = {
                                // To play the current list, we need to extract the loaded items
                                val loadedSongs = pagedSongs.itemSnapshotList.items
                                viewModel.processIntent(HomeIntent.PlaySong(loadedSongs, index))
                                playerViewModel.playSongList(loadedSongs, index)
                            }
                        )
                    }
                }

                // Show a mini loading spinner at the VERY BOTTOM if it is fetching the next page
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

@Composable
fun SongItemRow(song: Song, onClick: () -> Unit) {
    val dimens = LocalDimens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimens.small)
            .clip(RoundedCornerShape(dimens.small))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(dimens.medium)
    ) {
        AsyncImage(
            model = song.coverImageUrl,
            contentDescription = stringResource(id = R.string.content_desc_album_art),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(dimens.largeIcon)
                .clip(RoundedCornerShape(dimens.extraSmall))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.width(dimens.medium))

        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = song.artistName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}