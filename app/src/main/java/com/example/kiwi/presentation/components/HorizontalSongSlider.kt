package com.example.kiwi.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.kiwi.domain.model.Song
import com.example.kiwi.presentation.theme.LocalDimens

@Composable
fun HorizontalSongSlider(
    title: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    val dimens = LocalDimens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = dimens.medium, vertical = dimens.small)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = dimens.medium),
            horizontalArrangement = Arrangement.spacedBy(dimens.medium)
        ) {
            items(songs) { song ->
                Column(
                    modifier = Modifier
                        .width(dimens.huge) // Fixed width for square items
                        .clickable { onSongClick(song) }
                ) {
                    AsyncImage(
                        model = song.coverImageUrl,
                        contentDescription = "Cover for ${song.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(dimens.huge)
                            .clip(RoundedCornerShape(dimens.small))
                    )
                    Spacer(modifier = Modifier.height(dimens.extraSmall))
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artistName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}