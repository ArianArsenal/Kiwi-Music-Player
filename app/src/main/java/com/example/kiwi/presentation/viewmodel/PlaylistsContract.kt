package com.example.kiwi.presentation.viewmodel

import com.example.kiwi.domain.model.Playlist

data class PlaylistsUiState(
    val dummyState: Boolean = false
)

sealed class PlaylistsIntent {
    object LoadPlaylists : PlaylistsIntent()
}

sealed class PlaylistsEffect {
    // Reserved for future one-time events
}