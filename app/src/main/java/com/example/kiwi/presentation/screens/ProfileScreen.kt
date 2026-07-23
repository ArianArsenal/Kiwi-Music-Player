package com.example.kiwi.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import coil.compose.AsyncImage
import com.example.kiwi.R
import com.example.kiwi.presentation.viewmodel.SettingsViewModel
import com.example.kiwi.presentation.viewmodel.SocialViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit,
    onFindFriendsClick: () -> Unit,
    onLikedSongsClick: () -> Unit,
    onFollowersClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit,
    onRecentlyPlayedClick: () -> Unit,
    onTopArtistsClick: () -> Unit,
    onUsernameClick: (String) -> Unit,
    onMyPlaylistsClick: () -> Unit, // <-- ADDED
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel()
) {
    val settings by settingsViewModel.settingsState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val username = settings.username ?: "User"
    val premiumText = if (settings.isPremium) stringResource(id = R.string.premium_member) else stringResource(id = R.string.normal_user)

    val followerCount by socialViewModel.getFollowerCount(username).collectAsState(initial = 0)
    val followingCount by socialViewModel.getFollowingCount(username).collectAsState(initial = 0)

    var isUpgrading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val flag = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(it, flag)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            settingsViewModel.updateProfilePicture(it.toString())
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.profile_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable { launcher.launch("image/*") }
                ) {
                    if (settings.profilePicUri.isNullOrEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        AsyncImage(
                            model = settings.profilePicUri,
                            contentDescription = stringResource(id = R.string.content_desc_profile_picture),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = username,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onUsernameClick(username) } // <-- FIXED: Now uses onUsernameClick
                        )

                        if (settings.isPremium) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = stringResource(id = R.string.verified_premium),
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(premiumText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onFollowersClick(username) }
                            .padding(4.dp)
                        ) {
                            Text("$followerCount ", fontWeight = FontWeight.Bold)
                            Text(stringResource(id = R.string.followers), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onFollowingClick(username) }
                            .padding(4.dp)
                        ) {
                            Text("$followingCount ", fontWeight = FontWeight.Bold)
                            Text(stringResource(id = R.string.following), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (!settings.isPremium) {
            item {
                Button(
                    onClick = {
                        scope.launch {
                            isUpgrading = true
                            delay(2000)
                            settingsViewModel.upgradeToPremium()
                            isUpgrading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .animateContentSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isUpgrading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(id = R.string.upgrading), fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.upgrade_to_premium), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            ProfileMenuItem(
                icon = Icons.Default.Favorite,
                title = stringResource(id = R.string.liked_songs),
                onClick = onLikedSongsClick
            )
        }

        item {
            ProfileMenuItem(
                icon = Icons.Default.Search,
                title = stringResource(id = R.string.find_friends),
                onClick = onFindFriendsClick
            )
        }

        item {
            ProfileMenuItem(
                icon = Icons.Default.History,
                title = stringResource(id = R.string.recently_played),
                onClick = onRecentlyPlayedClick
            )
        }

        item {
            ProfileMenuItem(
                icon = Icons.Default.QueueMusic,
                title = stringResource(id = R.string.my_playlists),
                onClick = onMyPlaylistsClick
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}