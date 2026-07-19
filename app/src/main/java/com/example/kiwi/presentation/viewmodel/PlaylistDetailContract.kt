package com.example.kiwi.presentation.viewmodel


data class PlaylistDetailUiState(
    val playlistId: String = ""
)

sealed class PlaylistDetailIntent {
    data class LoadPlaylist(val playlistId: String) : PlaylistDetailIntent()
}
sealed class PlaylistDetailEffect