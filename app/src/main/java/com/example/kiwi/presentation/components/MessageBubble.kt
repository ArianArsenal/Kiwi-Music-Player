package com.example.kiwi.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kiwi.domain.model.ChatMessage
import com.example.kiwi.domain.model.MessageStatus
import com.example.kiwi.presentation.theme.LocalDimens

@Composable
fun MessageBubble(
    message: ChatMessage,
    isOwnMessage: Boolean,
    sharedSongTitle: String?,
    sharedSongArtist: String?,
    sharedSongCoverUrl: String?, // FIX: Added cover parameter
    onSongClick: (String) -> Unit
) {
    val dimens = LocalDimens.current
    val backgroundColor = if (isOwnMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOwnMessage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = dimens.medium,
                        topEnd = dimens.medium,
                        bottomStart = if (isOwnMessage) dimens.medium else 0.dp,
                        bottomEnd = if (isOwnMessage) 0.dp else dimens.medium
                    )
                )
                .background(backgroundColor)
                .padding(dimens.medium)
        ) {
            if (message.sharedSongId != null) {
                SongMiniCard(
                    text = message.text,
                    textColor = textColor,
                    songTitle = sharedSongTitle ?: "Unknown Track",
                    songArtist = sharedSongArtist ?: "Unknown Artist",
                    songCoverUrl = sharedSongCoverUrl, // FIX: Pass cover down
                    onClick = { onSongClick(message.sharedSongId) }
                )
            } else {
                Text(text = message.text, color = textColor, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isOwnMessage) {
                Row(modifier = Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (message.status) {
                        MessageStatus.SENDING -> Icons.Default.Schedule
                        MessageStatus.SENT -> Icons.Default.Check
                        MessageStatus.READ -> Icons.Default.DoneAll
                    }
                    val iconTint = if (message.status == MessageStatus.READ) Color.Cyan else textColor.copy(alpha = 0.7f)

                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun SongMiniCard(
    text: String,
    textColor: Color,
    songTitle: String,
    songArtist: String,
    songCoverUrl: String?,
    onClick: () -> Unit
) {
    val dimens = LocalDimens.current
    Column {
        if (text.isNotBlank()) {
            Text(text = text, color = textColor, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(dimens.small))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.small))
                .background(Color.Black.copy(alpha = 0.2f))
                .clickable { onClick() }
                .padding(dimens.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // FIX: Render actual Cover Art using Coil
            if (songCoverUrl != null) {
                AsyncImage(
                    model = songCoverUrl,
                    contentDescription = "Song Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = "Music", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(modifier = Modifier.width(dimens.small))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = songTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = songArtist,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}