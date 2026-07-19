package com.example.kiwi.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kiwi.domain.model.Playlist
import com.example.kiwi.presentation.theme.LocalDimens

@Composable
fun PlaylistCard(
    playlist: Playlist,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    val dimens = LocalDimens.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Makes the card a perfect square
            .clickable { onClick() },
        shape = RoundedCornerShape(dimens.medium),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Playlist Title
            Text(
                text = playlist.name, // Update to playlist.name if your model uses 'name'
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(dimens.small)
                    .weight(1f)
            )

            // Playlist Cover Image (bottom right corner style)
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = playlist.coverImageUrl,
                    contentDescription = "Playlist Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        // Round the top-left to look cool, and the bottom-right to match the Card's corner
                        .clip(
                            RoundedCornerShape(
                                topStart = dimens.medium,
                                bottomEnd = dimens.medium
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun ShimmerPlaylistCard() {
    val dimens = LocalDimens.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(dimens.medium))
            .shimmerEffect()
    )
}