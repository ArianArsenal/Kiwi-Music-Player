package com.example.kiwi.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val id: String,
    val name: String,
    @SerialName("cover_image_url")
    val coverImageUrl: String
)