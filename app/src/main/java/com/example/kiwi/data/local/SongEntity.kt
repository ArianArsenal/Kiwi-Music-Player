package com.example.kiwi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kiwi.domain.model.Song

@Entity // If you have (tableName = "something_else") here, update the DAO queries above!
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistName: String,
    val coverImageUrl: String,
    val audioUrl: String
)

// Helper functions to convert between Network data and Local database data
fun SongEntity.toDomain() = Song(id, title, artistName, coverImageUrl, audioUrl)
fun Song.toEntity() = SongEntity(id, title, artistName, coverImageUrl, audioUrl)