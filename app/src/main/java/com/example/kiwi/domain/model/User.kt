package com.example.kiwi.domain.model

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val followerCount: Int,
    val isFollowing: Boolean,
    val publicPlaylists: List<Playlist> = emptyList(),
    val profilePicUri: String? = null,
    val isPremium: Boolean = false
)