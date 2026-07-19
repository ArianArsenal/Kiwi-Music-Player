package com.example.kiwi.presentation.viewmodel

import com.example.kiwi.domain.model.Song

enum class SortOrder(val label: String) {
    TITLE_ASC("Title (A-Z)"),
    ARTIST_ASC("Artist (A-Z)"),
    NEWEST("Newest First")
}

data class DownloadsUiState(
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val downloadedSongs: List<Song> = emptyList(),
    val isLoading: Boolean = true
)

sealed class DownloadsIntent {
    data class ChangeSortOrder(val order: SortOrder) : DownloadsIntent()
    data class DeleteSong(val song: Song) : DownloadsIntent()
}

sealed class DownloadsEffect {
    data class ShowToast(val message: String) : DownloadsEffect()
}