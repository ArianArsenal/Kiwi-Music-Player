package com.example.kiwi.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val id: String,
    val name: String,
    @SerialName("cover_image_url")
    val coverImageUrl: String,
    @SerialName("user_id")
    val userId: String? = null,
    val category: String = "user",
    // NEW: Map the Supabase boolean
    @SerialName("is_public")
    val isPublic: Boolean = true
)