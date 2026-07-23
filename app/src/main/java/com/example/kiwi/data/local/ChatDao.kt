package com.example.kiwi.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class RecentChat(
    val friendId: String,
    val profilePicUri: String?,
    val lastMessageText: String,
    val lastMessageTime: Long,
    val unreadCount: Int
)

@Dao
interface ChatDao {
    @Query("""
        SELECT * FROM chat_messages 
        WHERE (senderId = :currentUserId AND receiverId = :friendId) 
           OR (senderId = :friendId AND receiverId = :currentUserId)
        ORDER BY timestamp DESC
    """)
    fun getChatHistoryWithUser(currentUserId: String, friendId: String): PagingSource<Int, ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET status = :newStatus WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, newStatus: String)

    @Query("""
        SELECT 
            contact_id AS friendId,
            users.profilePicUri,
            text AS lastMessageText,
            lastMessageTime,
            unreadCount
        FROM (
            SELECT 
                CASE WHEN senderId = :currentUserId THEN receiverId ELSE senderId END AS contact_id,
                text,
                MAX(timestamp) AS lastMessageTime,
                SUM(CASE WHEN receiverId = :currentUserId AND status != 'READ' THEN 1 ELSE 0 END) AS unreadCount
            FROM chat_messages
            WHERE senderId = :currentUserId OR receiverId = :currentUserId
            GROUP BY CASE WHEN senderId = :currentUserId THEN receiverId ELSE senderId END
        ) 
        LEFT JOIN users ON users.username = contact_id
        ORDER BY lastMessageTime DESC
    """)
    fun getRecentChats(currentUserId: String): Flow<List<RecentChat>>

    @Query("UPDATE chat_messages SET status = 'READ' WHERE receiverId = :currentUserId AND senderId = :friendId")
    suspend fun markMessagesAsRead(currentUserId: String, friendId: String)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE receiverId = :currentUserId AND status != 'READ'")
    fun getTotalUnreadCount(currentUserId: String): Flow<Int>
}