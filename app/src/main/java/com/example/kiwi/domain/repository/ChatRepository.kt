package com.example.kiwi.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
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
    fun getMessagesWithUser(currentUserId: String, friendId: String): Flow<PagingData<ChatMessage>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { chatDao.getChatHistoryWithUser(currentUserId, friendId) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
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