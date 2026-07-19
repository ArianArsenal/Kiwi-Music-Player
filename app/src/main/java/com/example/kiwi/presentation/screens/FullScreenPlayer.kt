package com.example.kiwi.presentation.screens

import android.graphics.Bitmap
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kiwi.presentation.components.AudioVisualizer
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.PlayerIntent
import com.example.kiwi.presentation.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun FullScreenPlayer(
    viewModel: PlayerViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val song = uiState.currentSong ?: return
    val dimens = LocalDimens.current
    val context = LocalContext.current

    // Dynamic Background Color Animation
    val targetColor = uiState.dominantColor?.let { Color(it) } ?: MaterialTheme.colorScheme.background
    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 1000),
        label = "bgColor"
    )

    // CD Rotation Animation
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
            // 1. Base solid color to ensure zero transparency
            .background(MaterialTheme.colorScheme.background)
            // 2. Fully opaque gradient on top
            .background(
                Brush.verticalGradient(
                    colors = listOf(animatedBackgroundColor, MaterialTheme.colorScheme.background)
                )
            )
            // 3. Swallow all clicks so you can't interact with the screen below
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 4. Push content above the Android system navigation buttons
                .systemBarsPadding()
                // 5. Make the page fully scrollable to fit everything on smaller screens
                .verticalScroll(rememberScrollState())
                .padding(dimens.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close")
                }
                Text("Now Playing", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterVertically))
                IconButton(onClick = { /* TODO: Context menu */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            }

            Spacer(modifier = Modifier.height(dimens.extraLarge))

            // Animated CD Cover with Palette Color Extraction
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .graphicsLayer { rotationZ = currentRotation }
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(song.coverImageUrl)
                        .allowHardware(false)
                        .build(),
                    contentDescription = "CD Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    onSuccess = { result ->
                        val bitmap: Bitmap = result.result.drawable.toBitmap()
                        Palette.from(bitmap).generate { palette ->
                            val color = palette?.vibrantSwatch?.rgb ?: palette?.mutedSwatch?.rgb
                            color?.let { viewModel.processIntent(PlayerIntent.UpdateDominantColor(it)) }
                        }
                    }
                )
                // The hole in the middle of the CD
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background))
            }

            Spacer(modifier = Modifier.height(dimens.large))

            // Canvas Audio Visualizer
            AudioVisualizer(isPlaying = uiState.isPlaying)

            // Replaced weight(1f) with a fixed spacer for the scrollable column
            Spacer(modifier = Modifier.height(dimens.extraLarge))


            // Song Info
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

            Spacer(modifier = Modifier.height(dimens.large))

            // Progress Slider
            Slider(
                value = uiState.progress,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            // Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.processIntent(PlayerIntent.SkipPrevious) }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(48.dp))
                }

                FloatingActionButton(
                    onClick = { viewModel.processIntent(PlayerIntent.TogglePlayPause) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(onClick = { viewModel.processIntent(PlayerIntent.SkipNext) }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(dimens.large))

            // Bottom Tools: Speed & Sleep Timer
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
                    Icon(Icons.Default.Timer, contentDescription = null, tint = if (timerActive) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (timerActive) "15m" else "Sleep Timer", color = if (timerActive) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                }
            }
        }
    }
}
