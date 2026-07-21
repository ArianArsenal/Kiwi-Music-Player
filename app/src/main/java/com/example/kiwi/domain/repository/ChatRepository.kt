package com.example.kiwi.domain.repository

import com.example.kiwi.data.local.ChatDao
import com.example.kiwi.domain.model.ChatMessage
import com.example.kiwi.domain.model.MessageStatus
import com.example.kiwi.data.local.toDomain
import com.example.kiwi.data.local.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao
) {
    // 1. UI observes this Flow. If the internet drops, the UI still shows the cached history.
    fun getMessagesWithUser(userId: String): Flow<List<ChatMessage>> {
        return chatDao.getChatHistoryWithUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // 2. Used when you hit 'Send', or when the WebSocket receives a message.
    suspend fun saveMessage(message: ChatMessage) {
        chatDao.insertMessage(message.toEntity())
    }

    // 3. Used when the WebSocket fires a MessageReceipt event (e.g., the double blue tick)
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        chatDao.updateMessageStatus(messageId, status.name)
    }
}