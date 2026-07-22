package com.example.kiwi.data.local

import androidx.room.Entity
import com.example.kiwi.domain.model.Song

@Entity(primaryKeys = ["id", "accountId"])
data class SongEntity(
    val id: String,
    val title: String,
    val artistName: String,
    val coverImageUrl: String,
    val audioUrl: String,
    val accountId: String
)

// Helper functions updated to handle the accountId
fun SongEntity.toDomain() = Song(id, title, artistName, coverImageUrl, audioUrl)
fun Song.toEntity(accountId: String) = SongEntity(id, title, artistName, coverImageUrl, audioUrl, accountId)