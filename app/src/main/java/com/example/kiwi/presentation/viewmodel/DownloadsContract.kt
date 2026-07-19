package com.example.kiwi.presentation.viewmodel

import com.example.kiwi.domain.model.Song

data class DownloadsUiState(
    val downloadedSongs: List<Song> = emptyList(),
    val isLoading: Boolean = false
)

sealed class DownloadsIntent {
    object LoadDownloads : DownloadsIntent()
}

sealed class DownloadsEffect {
    // Empty for now, but required for architecture consistency
}