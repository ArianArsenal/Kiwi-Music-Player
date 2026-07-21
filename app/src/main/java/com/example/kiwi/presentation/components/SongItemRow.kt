package com.example.kiwi.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.kiwi.domain.model.Song
import com.example.kiwi.presentation.theme.LocalDimens

@Composable
fun SongItemRow(
    song: Song,
    isPremium: Boolean = false, // Added premium check
    showDownloadIcon: Boolean = true,
    onClick: () -> Unit,
    onDownloadClick: (Song) -> Unit = {} // Added download action
) {
    val dimens = LocalDimens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = dimens.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverImageUrl,
            contentDescription = "Cover for ${song.title}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(dimens.extraLarge)
                .clip(RoundedCornerShape(dimens.small))
        )

        Spacer(modifier = Modifier.width(dimens.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artistName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (showDownloadIcon) {
            IconButton(onClick = { onDownloadClick(song) }) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download Song",
                    tint = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}

@Composable
fun ShimmerSongItemRow() {
    val dimens = LocalDimens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimens.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shimmering Cover Image
        Box(
            modifier = Modifier
                .size(dimens.extraLarge)
                .clip(RoundedCornerShape(dimens.small))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.width(dimens.medium))

        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(dimens.medium)
                    .clip(RoundedCornerShape(dimens.small))
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(dimens.extraSmall))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(dimens.small)
                    .clip(RoundedCornerShape(dimens.small))
                    .shimmerEffect()
            )
        }
    }
}