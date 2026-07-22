package com.example.kiwi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kiwi.domain.model.ChatMessage
import com.example.kiwi.domain.model.MessageStatus

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Long,
    val status: String,
    val sharedSongId: String?,
    val sharedSongTitle: String? = null,
    val sharedSongArtist: String? = null,
    val sharedSongCoverUrl: String? = null,
    val sharedSongAudioUrl: String? = null
)

// FIX: Pass all embedded fields to the domain model
fun ChatMessageEntity.toDomain() = ChatMessage(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    text = text,
    timestamp = timestamp,
    status = MessageStatus.valueOf(status),
    sharedSongId = sharedSongId,
    sharedSongTitle = sharedSongTitle,
    sharedSongArtist = sharedSongArtist,
    sharedSongCoverUrl = sharedSongCoverUrl,
    sharedSongAudioUrl = sharedSongAudioUrl
)

// FIX: Store all embedded fields into Room
fun ChatMessage.toEntity() = ChatMessageEntity(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    text = text,
    timestamp = timestamp,
    status = status.name,
    sharedSongId = sharedSongId,
    sharedSongTitle = sharedSongTitle,
    sharedSongArtist = sharedSongArtist,
    sharedSongCoverUrl = sharedSongCoverUrl,
    sharedSongAudioUrl = sharedSongAudioUrl
)