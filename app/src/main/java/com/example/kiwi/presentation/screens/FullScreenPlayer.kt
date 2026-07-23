package com.example.kiwi.presentation.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kiwi.R
import com.example.kiwi.presentation.components.AudioVisualizer
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.PlayerIntent
import com.example.kiwi.presentation.viewmodel.PlayerViewModel
import com.example.kiwi.worker.DownloadWorker
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FullScreenPlayer(
    viewModel: PlayerViewModel,
    onClose: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val uiState by viewModel.uiState.collectAsState()
    val song = uiState.currentSong ?: return
    val dimens = LocalDimens.current
    val context = LocalContext.current

    var showMenu by remember { mutableStateOf(false) }
    val isPremiumUser by viewModel.isPremium.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    val targetColor = uiState.dominantColor?.let { Color(it) } ?: MaterialTheme.colorScheme.background
    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 1000),
        label = "bgColor"
    )

    var currentRotation by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(uiState.isPlaying) {
        if (uiState.isPlaying) {
            while (true) {
                currentRotation += 2f
                if (currentRotation >= 360f) currentRotation = 0f
                delay(16L)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(
                Brush.verticalGradient(
                    colors = listOf(animatedBackgroundColor, MaterialTheme.colorScheme.background)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(dimens.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(id = R.string.close))
                }
                Text(
                    text = stringResource(id = R.string.now_playing),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.more))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.download)) },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                if (isPremiumUser) {
                                    val workData = Data.Builder()
                                        .putString("SONG_ID", song.id)
                                        .putString("AUDIO_URL", song.audioUrl)
                                        .putString("SONG_TITLE", song.title)
                                        .putString("ARTIST_NAME", song.artistName)
                                        .putString("COVER_URL", song.coverImageUrl)
                                        .putString("ACCOUNT_ID", currentUserId)
                                        .build()

                                    val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                                        .setInputData(workData).build()

                                    WorkManager.getInstance(context).enqueue(request)
                                    Toast.makeText(context, context.getString(R.string.downloading_song, song.title), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.upgrade_premium_download), Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.extraLarge))

            val imageModifier = Modifier
                .size(280.dp)
                .then(
                    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                state = rememberSharedContentState(key = "cover-${song.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    } else Modifier
                )
                .graphicsLayer { rotationZ = currentRotation }
                .clip(CircleShape)
                .background(Color.Black)

            Box(modifier = imageModifier, contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(song.coverImageUrl)
                        .allowHardware(false)
                        .build(),
                    contentDescription = stringResource(id = R.string.cd_cover),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    onSuccess = { result ->
                        val bitmap: Bitmap = result.result.drawable.toBitmap()
                        Palette.from(bitmap).generate { palette ->
                            val color = palette?.vibrantSwatch?.rgb ?: palette?.mutedSwatch?.rgb
                            color?.let { viewModel.processIntent(PlayerIntent.UpdateDominantColor(it)) }
                        }
                    }
                )
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background))
            }

            Spacer(modifier = Modifier.height(dimens.large))
            AudioVisualizer(isPlaying = uiState.isPlaying)
            Spacer(modifier = Modifier.height(dimens.extraLarge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = song.artistName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                IconButton(onClick = { viewModel.processIntent(PlayerIntent.ToggleLikeCurrentSong) }) {
                    Icon(
                        imageVector = if (uiState.isCurrentSongLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(id = R.string.like_song),
                        tint = if (uiState.isCurrentSongLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.large))

            fun formatTime(ms: Long): String {
                val totalSeconds = ms / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                return String.format("%02d:%02d", minutes, seconds)
            }

            // FIXED: Wrap all core playback controls in LTR composition
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = uiState.progress,
                            onValueChange = { newProgress -> viewModel.processIntent(PlayerIntent.SeekToFraction(newProgress)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(uiState.currentPositionMs),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                            Text(
                                text = formatTime(uiState.durationMs),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.processIntent(PlayerIntent.SkipPrevious) }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = stringResource(id = R.string.previous), modifier = Modifier.size(48.dp))
                        }

                        FloatingActionButton(
                            onClick = { viewModel.processIntent(PlayerIntent.TogglePlayPause) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = stringResource(id = R.string.play_pause),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        IconButton(onClick = { viewModel.processIntent(PlayerIntent.SkipNext) }) {
                            Icon(Icons.Default.SkipNext, contentDescription = stringResource(id = R.string.next), modifier = Modifier.size(48.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        val newSpeed = when (uiState.playbackSpeed) {
                            1.0f -> 1.5f
                            1.5f -> 2.0f
                            else -> 1.0f
                        }
                        viewModel.processIntent(PlayerIntent.SetPlaybackSpeed(newSpeed))
                    }
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${uiState.playbackSpeed}x")
                }

                var timerActive by remember { mutableStateOf(false) }
                TextButton(
                    onClick = {
                        timerActive = !timerActive
                        val minutes = if (timerActive) 15 else 0
                        viewModel.processIntent(PlayerIntent.SetSleepTimer(minutes))
                    }
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (timerActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (timerActive) stringResource(id = R.string.timer_15m) else stringResource(id = R.string.sleep_timer),
                        color = if (timerActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
            }
        }
    }
}