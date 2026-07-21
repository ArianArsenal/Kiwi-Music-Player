package com.example.kiwi.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kiwi.R
import com.example.kiwi.presentation.theme.LocalDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiwiTopBar(
    profilePicUri: String?, // <-- NEW PARAMETER
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onChatClick: () -> Unit
) {
    val dimens = LocalDimens.current

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.kiwi_logo),
                    contentDescription = stringResource(id = R.string.content_desc_app_logo),
                    modifier = Modifier
                        .size(dimens.extraLarge)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(dimens.small))
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.content_desc_settings),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onChatClick) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = stringResource(id = R.string.content_desc_direct_messages),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onProfileClick) {
                if (profilePicUri.isNullOrEmpty()) {
                    Surface(
                        modifier = Modifier.size(dimens.large),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(id = R.string.content_desc_profile_picture),
                            modifier = Modifier.padding(4.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                } else {
                    AsyncImage(
                        model = profilePicUri,
                        contentDescription = stringResource(id = R.string.content_desc_profile_picture),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(dimens.large)
                            .clip(CircleShape)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}