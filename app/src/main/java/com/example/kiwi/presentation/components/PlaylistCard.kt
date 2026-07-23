package com.example.kiwi.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kiwi.domain.model.Playlist

@Composable
fun PlaylistCard(
    playlist: Playlist,
    backgroundColor: Color, // Kept to prevent breaking PlaylistsScreen, but overridden visually
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // Forces a perfect square shape
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        // 1. Full-size background image
        AsyncImage(
            model = playlist.coverImageUrl,
            contentDescription = playlist.name,
            contentScale = ContentScale.Crop, // Crops the image to perfectly fill the square
            modifier = Modifier.fillMaxSize()
        )

        // 2. Dark gradient overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.9f) // Darkest at the bottom
                        ),
                        startY = 100f // Starts the gradient a bit lower down
                    )
                )
        )

        // 3. Playlist Name pinned to the bottom left
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )

        // 4. Lock Icon for Private Playlists
        if (!playlist.isPublic) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Private Playlist",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// FIX: Added the missing loading placeholder card so PlaylistsScreen compiles
@Composable
fun ShimmerPlaylistCard() {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}