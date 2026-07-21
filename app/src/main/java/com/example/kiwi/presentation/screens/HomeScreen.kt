package com.example.kiwi.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.kiwi.R
import com.example.kiwi.domain.model.Song
import com.example.kiwi.presentation.components.HorizontalSongSlider
import com.example.kiwi.presentation.components.QuickActionButton
import com.example.kiwi.presentation.components.ShimmerSongItemRow
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.HomeEffect
import com.example.kiwi.presentation.viewmodel.HomeIntent
import com.example.kiwi.presentation.viewmodel.HomeViewModel
import com.example.kiwi.presentation.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import com.example.kiwi.presentation.components.shimmerEffect

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel,
    onLikedSongsClick: () -> Unit,
    onPlaylistsClick: () -> Unit
) {
    val pagedSongs = viewModel.pagedSongs.collectAsLazyPagingItems()
    val dimens = LocalDimens.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowToast -> {
                    Toast.makeText(context, context.getString(effect.messageResId), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (pagedSongs.loadState.refresh is LoadState.Loading) {
            HomeSkeleton()
        } else {
            val loadedSongs = pagedSongs.itemSnapshotList.items.filterNotNull()

            if (loadedSongs.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = dimens.bottomNavPadding)
                ) {
                    item {
                        val carouselSongs = loadedSongs.take(3)
                        val pagerState = rememberPagerState(pageCount = { carouselSongs.size })

                        LaunchedEffect(pagerState) {
                            while (true) {
                                delay(4000)
                                val nextPage = (pagerState.currentPage + 1) % carouselSongs.size
                                pagerState.animateScrollToPage(nextPage)
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(dimens.medium)
                        ) { page ->
                            val song = carouselSongs[page]
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(dimens.medium))
                                    .clickable { playerViewModel.playSongList(carouselSongs, page) }
                            ) {
                                AsyncImage(
                                    model = song.coverImageUrl,
                                    contentDescription = stringResource(id = R.string.carousel_image),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                                startY = 100f
                                            )
                                        )
                                )
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(dimens.medium)
                                )
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimens.medium),
                            verticalArrangement = Arrangement.spacedBy(dimens.small)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimens.small)
                            ) {
                                QuickActionButton(
                                    icon = Icons.Default.Favorite,
                                    label = stringResource(id = R.string.liked_songs),
                                    onClick = onLikedSongsClick,
                                    modifier = Modifier.weight(1f)
                                )
                                QuickActionButton(
                                    icon = Icons.Default.PlayArrow,
                                    label = stringResource(id = R.string.recently_played),
                                    onClick = { Toast.makeText(context, "Coming Soon!", Toast.LENGTH_SHORT).show() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimens.small)
                            ) {
                                QuickActionButton(
                                    icon = Icons.Default.List,
                                    label = stringResource(id = R.string.my_playlists_home),
                                    onClick = onPlaylistsClick,
                                    modifier = Modifier.weight(1f)
                                )
                                QuickActionButton(
                                    icon = Icons.Default.Star,
                                    label = stringResource(id = R.string.top_artists),
                                    onClick = { Toast.makeText(context, "Coming Soon!", Toast.LENGTH_SHORT).show() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(dimens.large))
                    }

                    item {
                        val popularSongs = loadedSongs.drop(3).take(10)
                        if (popularSongs.isNotEmpty()) {
                            HorizontalSongSlider(
                                title = stringResource(id = R.string.most_popular),
                                songs = popularSongs,
                                onSongClick = { song ->
                                    playerViewModel.playSongList(popularSongs, popularSongs.indexOf(song))
                                }
                            )
                            Spacer(modifier = Modifier.height(dimens.large))
                        }
                    }

                    item {
                        val newestSongs = loadedSongs.drop(13).take(10)
                        if (newestSongs.isNotEmpty()) {
                            HorizontalSongSlider(
                                title = stringResource(id = R.string.new_releases),
                                songs = newestSongs,
                                onSongClick = { song ->
                                    playerViewModel.playSongList(newestSongs, newestSongs.indexOf(song))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun HomeSkeleton() {
    val dimens = LocalDimens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimens.medium, vertical = dimens.medium),
        verticalArrangement = Arrangement.spacedBy(dimens.large)
    ) {
        // 1. Carousel Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(dimens.medium))
                .shimmerEffect()
        )

        // 2. Quick Actions Skeleton
        Column(verticalArrangement = Arrangement.spacedBy(dimens.small)) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.small)) {
                Box(modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(dimens.small)).shimmerEffect())
                Box(modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(dimens.small)).shimmerEffect())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.small)) {
                Box(modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(dimens.small)).shimmerEffect())
                Box(modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(dimens.small)).shimmerEffect())
            }
        }

        // 3. Sliders Skeleton (Repeated twice for Popular & Newest)
        repeat(2) {
            Column {
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(dimens.small))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(dimens.medium))
                Row(horizontalArrangement = Arrangement.spacedBy(dimens.medium)) {
                    repeat(3) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(140.dp) // Matches our huge size
                                    .clip(RoundedCornerShape(dimens.small))
                                    .shimmerEffect()
                            )
                            Spacer(modifier = Modifier.height(dimens.extraSmall))
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(dimens.small))
                                    .shimmerEffect()
                            )
                        }
                    }
                }
            }
        }
    }
}