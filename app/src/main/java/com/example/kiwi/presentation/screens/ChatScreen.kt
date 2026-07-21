package com.example.kiwi.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.kiwi.presentation.components.MessageBubble
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.ChatViewModel
import com.example.kiwi.presentation.viewmodel.PlayerViewModel
import com.example.kiwi.presentation.viewmodel.DownloadsViewModel
import com.example.kiwi.presentation.viewmodel.PlayerIntent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    friendId: String, // <-- NEW
    friendName: String, // <-- NEW
    viewModel: ChatViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel,
    downloadsViewModel: DownloadsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {

    LaunchedEffect(friendId) {
        viewModel.setChatPartner(friendId)
    }
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isTyping by viewModel.isFriendTyping.collectAsState()

    var showSongPicker by remember { mutableStateOf(false) }
    val downloadsState by downloadsViewModel.uiState.collectAsState()

    val dimens = LocalDimens.current
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(friendName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // FIX: Only take the top padding, do NOT apply the bottom padding from Scaffold
                .padding(top = paddingValues.calculateTopPadding(), bottom = 8.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = dimens.medium)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = dimens.medium)
            ) {
                items(messages) { message ->
                    val sharedSong = message.sharedSongId?.let { id ->
                        downloadsState.downloadedSongs.find { it.id == id }
                    }

                    MessageBubble(
                        message = message,
                        isOwnMessage = message.senderId == viewModel.currentUserId,
                        sharedSongTitle = sharedSong?.title,
                        sharedSongArtist = "Downloaded Track",
                        sharedSongCoverUrl = sharedSong?.coverImageUrl,
                        onSongClick = {
                            if (sharedSong != null) {
                                // Replaced PlaySong with PlaySongList
                                playerViewModel.processIntent(PlayerIntent.PlaySongList(listOf(sharedSong), 0))
                            }
                        }
                    )
                }
            }

            AnimatedVisibility(visible = isTyping) {
                Text(
                    text = "Hossein is typing...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = dimens.small)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimens.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showSongPicker = true }) {
                    Icon(Icons.Default.Share, contentDescription = "Share Song", tint = MaterialTheme.colorScheme.primary)
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message...") },
                    shape = CircleShape,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(dimens.small))

                FloatingActionButton(
                    onClick = { viewModel.sendMessage() },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }

    if (showSongPicker) {
        ModalBottomSheet(onDismissRequest = { showSongPicker = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Share a Track", style = MaterialTheme.typography.titleLarge)
                Text("Select a downloaded song to send", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                if (downloadsState.downloadedSongs.isEmpty()) {
                    Text("No downloaded songs available to share.")
                } else {
                    LazyColumn {
                        items(downloadsState.downloadedSongs) { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.shareSong(song.id)
                                        showSongPicker = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // FIX: Display actual cover in the list
                                AsyncImage(
                                    model = song.coverImageUrl, // Change coverUri if your model uses a different name
                                    contentDescription = "Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Tap to share",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}