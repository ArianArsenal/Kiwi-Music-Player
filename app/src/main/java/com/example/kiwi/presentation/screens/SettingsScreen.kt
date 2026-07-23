package com.example.kiwi.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiwi.R
import com.example.kiwi.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settingsState.collectAsState()
    var languageMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) // <-- RTL FIX
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dark Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(id = R.string.dark_mode), style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = settings.isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode(it) }
                )
            }

            HorizontalDivider()

            // Language Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = stringResource(id = R.string.language), style = MaterialTheme.typography.titleMedium)
                }

                Box {
                    TextButton(onClick = { languageMenuExpanded = true }) {
                        Text(if (settings.language == "en") stringResource(id = R.string.english) else stringResource(id = R.string.persian))
                    }
                    DropdownMenu(
                        expanded = languageMenuExpanded,
                        onDismissRequest = { languageMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.english)) },
                            onClick = {
                                viewModel.setLanguage("en")
                                languageMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.persian)) },
                            onClick = {
                                viewModel.setLanguage("fa")
                                languageMenuExpanded = false
                            }
                        )
                    }
                }
            }

            if (settings.isPremium) {
                HorizontalDivider()
                Button(
                    onClick = { viewModel.cancelPremium() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(id = R.string.cancel_premium))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.log_out))
            }
        }
    }
}