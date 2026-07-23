package com.example.kiwi.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import coil.compose.AsyncImage
import com.example.kiwi.R
import com.example.kiwi.presentation.viewmodel.SocialViewModel
import com.example.kiwi.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    viewModel: SocialViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onPlaylistClick: (String) -> Unit,
    onMessageClick: (String, String) -> Unit,
    onFollowersClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit
) {
    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    val user by viewModel.viewedUser.collectAsState()
    val settingsState by settingsViewModel.settingsState.collectAsState()
    val currentUsername = settingsState.username ?: ""
    val isSelf = user?.username == currentUsername || userId == currentUsername

    val isFollowing by viewModel.getFollowStatus(userId).collectAsState(initial = false)
    val followerCount by viewModel.getFollowerCount(userId).collectAsState(initial = 0)
    val followingCount by viewModel.getFollowingCount(userId).collectAsState(initial = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (user!!.profilePicUri.isNullOrEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(id = R.string.avatar),
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    AsyncImage(
                        model = user!!.profilePicUri,
                        contentDescription = stringResource(id = R.string.avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(40.dp))
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = user!!.displayName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = stringResource(id = R.string.username_format, user!!.username),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onFollowersClick(user!!.id) }
                            .padding(4.dp)
                        ) {
                            Text("$followerCount ", fontWeight = FontWeight.Bold)
                            Text(stringResource(id = R.string.followers), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onFollowingClick(user!!.id) }
                            .padding(4.dp)
                        ) {
                            Text("$followingCount ", fontWeight = FontWeight.Bold)
                            Text(stringResource(id = R.string.following), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Hide Follow and Message actions if inspecting self-profile
            if (!isSelf) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.toggleFollow(user!!.id, isFollowing) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                            contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(if (isFollowing) stringResource(id = R.string.btn_following) else stringResource(id = R.string.btn_follow))
                    }

                    OutlinedButton(
                        onClick = { onMessageClick(user!!.id, user!!.displayName) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(id = R.string.btn_message))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = stringResource(id = R.string.public_playlists), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (user!!.publicPlaylists.isEmpty()) {
                Text(stringResource(id = R.string.no_public_playlists), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn {
                    items(user!!.publicPlaylists) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPlaylistClick(playlist.id) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = playlist.coverImageUrl.ifEmpty { "https://picsum.photos/200" },
                                contentDescription = stringResource(id = R.string.playlist_cover),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(id = R.string.by_author, user!!.displayName),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}