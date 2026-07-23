package com.example.kiwi.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // <-- THIS IMPORT WAS MISSING
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.kiwi.R
import com.example.kiwi.domain.model.Song
import com.example.kiwi.presentation.components.MessageBubble
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.ChatViewModel
import com.example.kiwi.presentation.viewmodel.PlayerIntent
import com.example.kiwi.presentation.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    friendId: String,
    friendName: String,
    viewModel: ChatViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel,
    onBackClick: () -> Unit
) {
    LaunchedEffect(friendId) {
        viewModel.setChatPartner(friendId)
    }

    val lazyPagingMessages = viewModel.messages.collectAsLazyPagingItems()
    val inputText by viewModel.inputText.collectAsState()
    val isTyping by viewModel.isFriendTyping.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    var showSongPicker by remember { mutableStateOf(false) }
    val songSearchQuery by viewModel.songSearchQuery.collectAsState()
    val songSearchResults by viewModel.songSearchResults.collectAsState()

    val dimens = LocalDimens.current
    val listState = rememberLazyListState()

    val shareMessageText = stringResource(id = R.string.check_out_this_song)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(friendName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding(), bottom = 8.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = dimens.medium)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = dimens.medium)
            ) {
                items(
                    count = lazyPagingMessages.itemCount,
                    key = { index -> lazyPagingMessages[index]?.id ?: index }
                ) { index ->
                    val message = lazyPagingMessages[index]
                    if (message != null) {
                        val songTitle = message.sharedSongTitle ?: "Unknown Track"
                        val songArtist = message.sharedSongArtist ?: "Shared Song"
                        val songCover = message.sharedSongCoverUrl
                        val songAudioUrl = message.sharedSongAudioUrl

                        MessageBubble(
                            message = message,
                            isOwnMessage = message.senderId == currentUserId,
                            sharedSongTitle = if (message.sharedSongId != null) songTitle else null,
                            sharedSongArtist = songArtist,
                            sharedSongCoverUrl = songCover,
                            onSongClick = {
                                if (songAudioUrl != null) {
                                    val playableSong = Song(
                                        id = message.sharedSongId ?: java.util.UUID.randomUUID().toString(),
                                        title = songTitle,
                                        artistName = songArtist,
                                        coverImageUrl = songCover ?: "",
                                        audioUrl = songAudioUrl
                                    )
                                    playerViewModel.processIntent(PlayerIntent.PlaySongList(listOf(playableSong), 0))
                                }
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isTyping) {
                Text(
                    text = stringResource(id = R.string.is_typing, friendName),
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
                    Icon(Icons.Default.Share, contentDescription = stringResource(id = R.string.share_song), tint = MaterialTheme.colorScheme.primary)
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(id = R.string.message_hint)) },
                    shape = CircleShape,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(dimens.small))

                FloatingActionButton(
                    onClick = { viewModel.sendMessage() },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(id = R.string.send))
                }
            }
        }
    }

    if (showSongPicker) {
        ModalBottomSheet(onDismissRequest = { showSongPicker = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.share_a_track),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = songSearchQuery,
                    onValueChange = { viewModel.updateSongSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(id = R.string.search_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (songSearchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(id = R.string.no_results_found), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 350.dp)
                    ) {
                        items(songSearchResults) { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.shareSong(song, shareMessageText)
                                        showSongPicker = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = song.coverImageUrl,
                                    contentDescription = stringResource(id = R.string.cover),
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
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artistName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}