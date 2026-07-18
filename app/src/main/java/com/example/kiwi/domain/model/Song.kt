package com.example.kiwi.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val id: String,
    val title: String,
    @SerialName("artist_name") // Maps the Kotlin camelCase to SQL snake_case
    val artistName: String,
    @SerialName("cover_image_url")
    val coverImageUrl: String,
    @SerialName("audio_url")
    val audioUrl: String
)