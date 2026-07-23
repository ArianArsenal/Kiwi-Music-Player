package com.example.kiwi.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.kiwi.R
import com.example.kiwi.presentation.components.PlaylistCard
import com.example.kiwi.presentation.components.ShimmerPlaylistCard
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.PlaylistsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    viewModel: PlaylistsViewModel = hiltViewModel(),
    onPlaylistClick: (String) -> Unit
) {
    val worldMusicPaging = viewModel.worldPlaylists.collectAsLazyPagingItems()
    val localMusicPaging = viewModel.localPlaylists.collectAsLazyPagingItems()
    val userPlaylistsPaging = viewModel.myPlaylists.collectAsLazyPagingItems()

    val dimens = LocalDimens.current
    val context = LocalContext.current

    val cardColors = listOf(
        Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
        Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4DB6AC)
    )

    // Dialog State
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var newPlaylistCoverUri by remember { mutableStateOf<Uri?>(null) }
    var isPublic by remember { mutableStateOf(true) }
    var isCreating by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val flag = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(it, flag)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            newPlaylistCoverUri = it
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { if (!isCreating) showCreateDialog = false },
            title = { Text(text = "Create Playlist", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { if (!isCreating) launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (newPlaylistCoverUri != null) {
                            AsyncImage(
                                model = newPlaylistCoverUri,
                                contentDescription = "Selected Cover",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tap to select cover")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isCreating
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Public Playlist", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = isPublic,
                            onCheckedChange = { isPublic = it },
                            enabled = !isCreating,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4CAF50)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isCreating = true

                        val bytes = newPlaylistCoverUri?.let { uri ->
                            try {
                                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            } catch (e: Exception) {
                                null
                            }
                        }

                        viewModel.createNewPlaylist(
                            name = newPlaylistName,
                            coverBytes = bytes,
                            isPublic = isPublic
                        ) {
                            isCreating = false
                            newPlaylistName = ""
                            newPlaylistCoverUri = null
                            isPublic = true
                            showCreateDialog = false
                            userPlaylistsPaging.refresh()
                        }
                    },
                    enabled = newPlaylistName.isNotBlank() && !isCreating
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }, enabled = !isCreating) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.playlists_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Playlist", tint = Color(0xFF4CAF50)) // Green + button
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (worldMusicPaging.loadState.refresh is LoadState.Loading) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = dimens.medium, vertical = dimens.small),
                    horizontalArrangement = Arrangement.spacedBy(dimens.medium),
                    verticalArrangement = Arrangement.spacedBy(dimens.medium)
                ) {
                    items(8) { ShimmerPlaylistCard() }
                }
            } else {
                val worldMusic = worldMusicPaging.itemSnapshotList.items.filterNotNull()
                val localMusic = localMusicPaging.itemSnapshotList.items.filterNotNull()
                val userPlaylists = userPlaylistsPaging.itemSnapshotList.items.filterNotNull()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = dimens.medium,
                        end = dimens.medium,
                        bottom = dimens.bottomNavPadding
                    ),
                    horizontalArrangement = Arrangement.spacedBy(dimens.medium),
                    verticalArrangement = Arrangement.spacedBy(dimens.medium)
                ) {
                    // --- World Music ---
                    item(span = { GridItemSpan(2) }) {
                        CategoryHeader(stringResource(id = R.string.world_music))
                    }
                    if (worldMusic.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            EmptyStateText("No world music playlists available.")
                        }
                    } else {
                        itemsIndexed(worldMusic) { index, playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                backgroundColor = cardColors[index % cardColors.size],
                                onClick = { onPlaylistClick(playlist.id) }
                            )
                        }
                    }

                    // --- Local Music ---
                    item(span = { GridItemSpan(2) }) {
                        CategoryHeader(stringResource(id = R.string.local_music))
                    }
                    if (localMusic.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            EmptyStateText("No local music playlists available.")
                        }
                    } else {
                        itemsIndexed(localMusic) { index, playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                backgroundColor = cardColors[(index + 2) % cardColors.size],
                                onClick = { onPlaylistClick(playlist.id) }
                            )
                        }
                    }

                    // --- Your Playlists ---
                    item(span = { GridItemSpan(2) }) {
                        CategoryHeader(stringResource(id = R.string.your_playlists))
                    }
                    if (userPlaylists.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            EmptyStateText("You haven't created any playlists yet. Tap the + to start!")
                        }
                    } else {
                        itemsIndexed(userPlaylists) { index, playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                backgroundColor = cardColors[(index + 4) % cardColors.size],
                                onClick = { onPlaylistClick(playlist.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    val dimens = LocalDimens.current
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = dimens.medium, bottom = dimens.small)
    )
}

@Composable
fun EmptyStateText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        modifier = Modifier.padding(bottom = 16.dp)
    )
}