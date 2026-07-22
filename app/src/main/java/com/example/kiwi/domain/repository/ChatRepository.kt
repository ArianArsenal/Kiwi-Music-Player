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
    // UI observes this Flow. Requires both the active user and the friend.
    fun getMessagesWithUser(currentUserId: String, friendId: String): Flow<List<ChatMessage>> {
        return chatDao.getChatHistoryWithUser(currentUserId, friendId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveMessage(message: ChatMessage) {
        chatDao.insertMessage(message.toEntity())
    }

    suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        chatDao.updateMessageStatus(messageId, status.name)
    }

    suspend fun markMessagesAsRead(currentUserId: String, friendId: String) {
        chatDao.markMessagesAsRead(currentUserId, friendId)
    }

    fun getTotalUnreadCount(currentUserId: String): Flow<Int> {
        return chatDao.getTotalUnreadCount(currentUserId)
    }
}