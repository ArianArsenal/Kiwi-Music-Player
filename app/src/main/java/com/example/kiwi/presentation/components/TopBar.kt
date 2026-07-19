package com.example.kiwi.presentation.components

import androidx.compose.foundation.Image // NEW IMPORT
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
import androidx.compose.ui.res.painterResource // NEW IMPORT
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.kiwi.R
import com.example.kiwi.presentation.theme.LocalDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiwiTopBar() {
    val dimens = LocalDimens.current

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // FIXED: Now pulling your actual generated App Icon!
                Image(
                    // CHANGED from R.mipmap.ic_launcher_round to R.drawable.app_logo
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
            IconButton(onClick = { /* TODO: Open Settings */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.content_desc_settings),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = { /* TODO: Open Notifications */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = stringResource(id = R.string.content_desc_notifications),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            // We leave this one as a placeholder until we fetch the actual user's avatar from Supabase
            Box(
                modifier = Modifier
                    .size(dimens.extraLarge)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(dimens.medium))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}