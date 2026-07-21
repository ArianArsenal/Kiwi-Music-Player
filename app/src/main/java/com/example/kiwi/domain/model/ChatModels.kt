package com.example.kiwi.domain.model

enum class MessageStatus { SENDING, SENT, READ }

data class ChatMessage(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Long,
    val status: MessageStatus,
    val sharedSongId: String? = null // Null for normal texts, holds an ID if a song is shared
)

// The sealed class that will handle our real-time WebSocket payloads
sealed class WebSocketEvent {
    data class IncomingMessage(val message: ChatMessage) : WebSocketEvent()
    data class TypingStatus(val userId: String, val isTyping: Boolean) : WebSocketEvent()
    data class MessageReceipt(val messageId: String, val status: MessageStatus) : WebSocketEvent()
}