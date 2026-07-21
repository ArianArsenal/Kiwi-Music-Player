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
    val status: String, // Room doesn't store Enums easily, so we save the String name
    val sharedSongId: String?
)

// Mapping extensions
fun ChatMessageEntity.toDomain() = ChatMessage(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    text = text,
    timestamp = timestamp,
    status = MessageStatus.valueOf(status),
    sharedSongId = sharedSongId
)

fun ChatMessage.toEntity() = ChatMessageEntity(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    text = text,
    timestamp = timestamp,
    status = status.name,
    sharedSongId = sharedSongId
)