package com.example.kiwi.data.repository

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistSongDTO(
    val playlist_id: String,
    val song_id: String
)