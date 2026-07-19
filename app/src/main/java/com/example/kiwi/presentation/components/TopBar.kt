package com.example.kiwi.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.kiwi.R
import com.example.kiwi.presentation.theme.LocalDimens

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiwiTopBar(
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
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
            IconButton(onClick = { /* TODO: Notifications */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = stringResource(id = R.string.content_desc_notifications),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            // Real Profile Image using Coil
            IconButton(onClick = onProfileClick) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop",
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(dimens.large)
                        .clip(CircleShape)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}