package com.example.kiwi.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.kiwi.R
import com.example.kiwi.presentation.theme.LocalDimens
import com.example.kiwi.presentation.viewmodel.ProfileIntent
import com.example.kiwi.presentation.viewmodel.ProfileViewModel
import androidx.compose.material.icons.filled.Search
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit = {},
    onFindFriendsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val dimens = LocalDimens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimens.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.profile_title), // DYNAMIC STRING
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimens.large)
        )

        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = uiState.avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable { viewModel.processIntent(ProfileIntent.ChangeAvatar) }
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { viewModel.processIntent(ProfileIntent.ChangeAvatar) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(dimens.medium))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = uiState.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (uiState.isPremium) {
                Spacer(modifier = Modifier.width(dimens.small))
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(dimens.large))

        AnimatedVisibility(visible = !uiState.isPremium) {
            Column {
                Button(
                    onClick = { viewModel.processIntent(ProfileIntent.UpgradeToPremium) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                    shape = RoundedCornerShape(dimens.medium)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(dimens.small))
                    Text(
                        text = stringResource(id = R.string.upgrade_premium), // DYNAMIC STRING
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(dimens.large))
            }
        }

        SettingsRow(
            icon = Icons.Default.Search,
            title = "Find Friends",
            onClick = onFindFriendsClick
        )

        // DYNAMIC STRINGS
        SettingsRow(
            icon = Icons.Default.Settings,
            title = stringResource(id = R.string.app_settings),
            onClick = onSettingsClick
        )
        SettingsRow(icon = Icons.Default.Language, title = stringResource(id = R.string.change_language))
        SettingsRow(icon = Icons.Default.Palette, title = stringResource(id = R.string.theme))
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit = {}
) {
    val dimens = LocalDimens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.small))
            .clickable { onClick() }
            .padding(vertical = dimens.medium, horizontal = dimens.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(dimens.medium))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }


}